package org.dromara.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.BasePersonnel;
import org.dromara.system.domain.bo.BasePersonnelBo;
import org.dromara.system.domain.vo.BasePersonnelVo;
import org.dromara.system.domain.vo.PersonnelRosterImportResultVo;
import org.dromara.system.mapper.BasePersonnelMapper;
import org.dromara.system.service.IBasePersonnelService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class BasePersonnelServiceImpl implements IBasePersonnelService {

    private final BasePersonnelMapper baseMapper;

    @Override
    public TableDataInfo<BasePersonnelVo> queryPageList(BasePersonnelBo bo, PageQuery pageQuery) {
        Page<BasePersonnelVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public List<BasePersonnelVo> queryList(BasePersonnelBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public BasePersonnelVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public BasePersonnelVo queryByJobNo(String jobNo) {
        return baseMapper.selectVoOne(new LambdaQueryWrapper<BasePersonnel>()
            .eq(BasePersonnel::getJobNo, jobNo)
            .eq(BasePersonnel::getStatus, SystemConstants.NORMAL), false);
    }

    @Override
    public boolean insertByBo(BasePersonnelBo bo) {
        validateUniqueJobNo(bo);
        normalize(bo);
        BasePersonnel entity = MapstructUtils.convert(bo, BasePersonnel.class);
        return baseMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateByBo(BasePersonnelBo bo) {
        validateUniqueJobNo(bo);
        normalize(bo);
        BasePersonnel entity = MapstructUtils.convert(bo, BasePersonnel.class);
        return baseMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteWithValidByIds(List<Long> ids) {
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    public String importData(List<BasePersonnelVo> rows, Boolean updateSupport) {
        int success = 0;
        int failure = 0;
        StringBuilder failureMsg = new StringBuilder();
        for (BasePersonnelVo row : rows) {
            try {
                if (StringUtils.isBlank(row.getJobNo()) || StringUtils.isBlank(row.getPersonName())) {
                    throw new ServiceException("工号和姓名不能为空");
                }
                BasePersonnelVo existing = baseMapper.selectVoOne(new LambdaQueryWrapper<BasePersonnel>()
                    .eq(BasePersonnel::getJobNo, row.getJobNo()), false);
                BasePersonnelBo bo = BeanUtil.toBean(row, BasePersonnelBo.class);
                if (existing == null) {
                    insertByBo(bo);
                } else if (Boolean.TRUE.equals(updateSupport)) {
                    bo.setId(existing.getId());
                    updateByBo(bo);
                } else {
                    throw new ServiceException("工号已存在");
                }
                success++;
            } catch (Exception e) {
                failure++;
                failureMsg.append("<br/>第 ").append(failure).append(" 条：")
                    .append(row.getJobNo()).append(" 导入失败：").append(e.getMessage());
            }
        }
        if (failure > 0) {
            throw new ServiceException("人员基础数据导入失败 " + failure + " 条：" + failureMsg);
        }
        return "人员基础数据导入成功 " + success + " 条";
    }

    @Override
    public PersonnelRosterImportResultVo importRoster(MultipartFile file, String sheetName) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("请选择需要导入的人员执行名单 Excel 文件");
        }
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            DataFormatter formatter = new DataFormatter();
            SheetMatch sheetMatch = findRosterSheet(workbook, formatter, sheetName);
            if (sheetMatch == null) {
                throw new ServiceException("未识别到包含工号、姓名、部门表头的执行名单 sheet");
            }
            PersonnelRosterImportResultVo result = new PersonnelRosterImportResultVo();
            result.setSheetName(sheetMatch.sheet.getSheetName());
            result.setHeaderRowIndex(sheetMatch.headerRowIndex + 1);
            Map<String, Integer> header = readHeader(sheetMatch.sheet.getRow(sheetMatch.headerRowIndex), formatter);
            for (int rowIndex = sheetMatch.headerRowIndex + 1; rowIndex <= sheetMatch.sheet.getLastRowNum(); rowIndex++) {
                Row row = sheetMatch.sheet.getRow(rowIndex);
                if (row == null || isBlankRow(row, formatter)) {
                    continue;
                }
                result.setTotalRows(result.getTotalRows() + 1);
                try {
                    BasePersonnel entity = toRosterPersonnel(row, header, formatter);
                    if (StringUtils.isBlank(entity.getJobNo()) || StringUtils.isBlank(entity.getPersonName())) {
                        throw new ServiceException("工号和姓名不能为空");
                    }
                    BasePersonnel existing = baseMapper.selectOne(new LambdaQueryWrapper<BasePersonnel>()
                        .eq(BasePersonnel::getJobNo, entity.getJobNo()), false);
                    if (existing == null) {
                        entity.setId(IdGeneratorUtil.nextLongId());
                        entity.setDelFlag("0");
                        entity.setStatus(SystemConstants.NORMAL);
                        entity.setCreateDept(LoginHelper.getDeptId());
                        entity.setCreateBy(LoginHelper.getUserId());
                        entity.setCreateTime(DateUtils.getNowDate());
                        baseMapper.insert(entity);
                        result.setInsertedRows(result.getInsertedRows() + 1);
                    } else {
                        entity.setId(existing.getId());
                        entity.setStatus(StringUtils.blankToDefault(existing.getStatus(), SystemConstants.NORMAL));
                        entity.setDelFlag(StringUtils.blankToDefault(existing.getDelFlag(), "0"));
                        entity.setUpdateBy(LoginHelper.getUserId());
                        entity.setUpdateTime(DateUtils.getNowDate());
                        baseMapper.updateById(entity);
                        result.setUpdatedRows(result.getUpdatedRows() + 1);
                    }
                } catch (Exception e) {
                    result.setFailedRows(result.getFailedRows() + 1);
                    result.getErrors().add("第 " + (rowIndex + 1) + " 行：" + e.getMessage());
                }
            }
            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("人员执行名单导入失败：" + e.getMessage());
        }
    }

    private LambdaQueryWrapper<BasePersonnel> buildQueryWrapper(BasePersonnelBo bo) {
        LambdaQueryWrapper<BasePersonnel> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(bo.getJobNo()), BasePersonnel::getJobNo, bo.getJobNo())
            .like(StringUtils.isNotBlank(bo.getPersonName()), BasePersonnel::getPersonName, bo.getPersonName())
            .like(StringUtils.isNotBlank(bo.getDeptName()), BasePersonnel::getDeptName, bo.getDeptName())
            .like(StringUtils.isNotBlank(bo.getWorkshop()), BasePersonnel::getWorkshop, bo.getWorkshop())
            .like(StringUtils.isNotBlank(bo.getTeamName()), BasePersonnel::getTeamName, bo.getTeamName())
            .like(StringUtils.isNotBlank(bo.getGuideGroup()), BasePersonnel::getGuideGroup, bo.getGuideGroup())
            .eq(bo.getDeptId() != null, BasePersonnel::getDeptId, bo.getDeptId())
            .eq(StringUtils.isNotBlank(bo.getStatus()), BasePersonnel::getStatus, bo.getStatus())
            .orderByDesc(BasePersonnel::getCreateTime);
        return wrapper;
    }

    private void validateUniqueJobNo(BasePersonnelBo bo) {
        boolean exists = baseMapper.exists(new LambdaQueryWrapper<BasePersonnel>()
            .eq(BasePersonnel::getJobNo, bo.getJobNo())
            .ne(bo.getId() != null, BasePersonnel::getId, bo.getId()));
        if (exists) {
            throw new ServiceException("工号已存在");
        }
    }

    private void normalize(BasePersonnelBo bo) {
        if (StringUtils.isBlank(bo.getStatus())) {
            bo.setStatus(SystemConstants.NORMAL);
        }
    }

    private SheetMatch findRosterSheet(Workbook workbook, DataFormatter formatter, String sheetName) {
        SheetMatch fallback = null;
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (StringUtils.isNotBlank(sheetName) && !StringUtils.equals(sheet.getSheetName(), sheetName)) {
                continue;
            }
            if (StringUtils.isBlank(sheetName) && sheet.getSheetName().contains("减员")) {
                continue;
            }
            Integer headerRowIndex = findRosterHeaderRow(sheet, formatter);
            if (headerRowIndex == null) {
                continue;
            }
            SheetMatch match = new SheetMatch(sheet, headerRowIndex);
            String title = cell(sheet.getRow(Math.max(0, headerRowIndex - 1)), 0, formatter);
            if (StringUtils.isNotBlank(sheetName) || title.contains("执行名单")) {
                return match;
            }
            if (fallback == null) {
                fallback = match;
            }
        }
        return fallback;
    }

    private Integer findRosterHeaderRow(Sheet sheet, DataFormatter formatter) {
        int max = Math.min(sheet.getLastRowNum(), 12);
        for (int i = 0; i <= max; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            String joined = joinRow(row, formatter);
            if (joined.contains("工号") && joined.contains("姓名") && joined.contains("部门")) {
                return i;
            }
        }
        return null;
    }

    private Map<String, Integer> readHeader(Row row, DataFormatter formatter) {
        Map<String, Integer> header = new LinkedHashMap<>();
        if (row == null) {
            return header;
        }
        for (int i = 0; i < row.getLastCellNum(); i++) {
            String value = cell(row, i, formatter);
            if (StringUtils.isNotBlank(value)) {
                header.put(value.trim(), i);
            }
        }
        return header;
    }

    private BasePersonnel toRosterPersonnel(Row row, Map<String, Integer> header, DataFormatter formatter) {
        BasePersonnel entity = new BasePersonnel();
        entity.setJobNo(value(row, header, formatter, "工号"));
        entity.setPersonName(value(row, header, formatter, "姓名"));
        entity.setNormalizedName(normalizePersonName(entity.getPersonName()));
        entity.setNation(value(row, header, formatter, "民族"));
        entity.setLineName(value(row, header, formatter, "线别"));
        entity.setDeptName(value(row, header, formatter, "部门"));
        entity.setJobTitle(value(row, header, formatter, "职名"));
        entity.setPositionName(value(row, header, formatter, "现岗位"));
        entity.setCurrentPosition(value(row, header, formatter, "现岗位"));
        entity.setPhone(value(row, header, formatter, "联系方式"));
        entity.setCommandTime(value(row, header, formatter, "命令时间"));
        entity.setPostTime(value(row, header, formatter, "顶岗时间"));
        entity.setPoliticalStatus(value(row, header, formatter, "政治面貌"));
        entity.setQualification(value(row, header, formatter, "资格"));
        entity.setPermittedLocomotiveType(value(row, header, formatter, "准驾机型"));
        entity.setBirthDate(value(row, header, formatter, "出生日期"));
        entity.setWorkStartDate(value(row, header, formatter, "参加工作"));
        entity.setIdCard(value(row, header, formatter, "身份证"));
        entity.setWorkCardNo(value(row, header, formatter, "工作证号"));
        entity.setRemark(value(row, header, formatter, "备注"));
        entity.setRawJson(JsonUtils.toJsonString(rawRow(row, header, formatter)));
        return entity;
    }

    private Map<String, String> rawRow(Row row, Map<String, Integer> header, DataFormatter formatter) {
        Map<String, String> raw = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : header.entrySet()) {
            raw.put(entry.getKey(), cell(row, entry.getValue(), formatter));
        }
        return raw;
    }

    private String value(Row row, Map<String, Integer> header, DataFormatter formatter, String name) {
        Integer index = header.get(name);
        if (index == null) {
            return "";
        }
        return cell(row, index, formatter);
    }

    private String normalizePersonName(String personName) {
        if (StringUtils.isBlank(personName)) {
            return "";
        }
        return personName.replaceFirst("^\\d+", "");
    }

    private String joinRow(Row row, DataFormatter formatter) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            builder.append(cell(row, i, formatter));
        }
        return builder.toString();
    }

    private boolean isBlankRow(Row row, DataFormatter formatter) {
        return StringUtils.isBlank(joinRow(row, formatter));
    }

    private String cell(Row row, int index, DataFormatter formatter) {
        if (row == null || index < 0 || row.getCell(index) == null) {
            return "";
        }
        return formatter.formatCellValue(row.getCell(index)).trim();
    }

    private record SheetMatch(Sheet sheet, int headerRowIndex) {
    }
}
