CALL CSVWRITE('/usr/share/tomcat5/ipt-data/backup/app_user.txt', 'select * from app_user');
CALL CSVWRITE('/usr/share/tomcat5/ipt-data/backup/user_role.txt', 'select * from user_role');
CALL CSVWRITE('/usr/share/tomcat5/ipt-data/backup/provider_cfg.txt', 'select * from provider_cfg');