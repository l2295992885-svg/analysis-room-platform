UPDATE sys_user
SET password_hash = '$2a$10$CWPPQkfOZc2rCXEBwOtMauYJRwEvfo7oWnzzyyn4Y1AD9yrD84i2i',
    updated_time = CURRENT_TIMESTAMP,
    remark = '开发环境管理员密码已更新为 BCrypt hash，默认密码仅用于本地联调'
WHERE username = 'admin'
  AND deleted = 0;
