-- ============================================================
-- X公司人事管理系统 数据库结构（MySQL 8.0，utf8mb4）
-- 说明：脚本可重复执行（IF NOT EXISTS），演示数据见 data.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS salary_grade (
    grade_code  VARCHAR(8)     NOT NULL COMMENT '等级编码',
    grade_name  VARCHAR(40)    NOT NULL COMMENT '等级名称',
    base_salary DECIMAL(12, 2) NOT NULL COMMENT '基本工资',
    description VARCHAR(200)            DEFAULT NULL COMMENT '说明',
    sort_no     INT            NOT NULL COMMENT '排序',
    PRIMARY KEY (grade_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '薪酬等级字典';

CREATE TABLE IF NOT EXISTS employee (
    id                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    emp_no            VARCHAR(16)   NOT NULL COMMENT '员工编号',
    name              VARCHAR(40)   NOT NULL COMMENT '姓名',
    gender            CHAR(1)       NOT NULL COMMENT 'M/F',
    birth_date        DATE                   DEFAULT NULL COMMENT '出生日期',
    id_card           VARCHAR(18)            DEFAULT NULL COMMENT '身份证号',
    phone             VARCHAR(20)            DEFAULT NULL COMMENT '手机号',
    email             VARCHAR(80)            DEFAULT NULL COMMENT '邮箱',
    address           VARCHAR(200)           DEFAULT NULL COMMENT '住址',
    department        VARCHAR(40)   NOT NULL COMMENT '部门',
    position          VARCHAR(40)   NOT NULL COMMENT '职位',
    grade_code        VARCHAR(8)    NOT NULL COMMENT '薪酬等级',
    hire_date         DATE          NOT NULL COMMENT '入职日期',
    status            VARCHAR(10)   NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/RESIGNED',
    resign_date       DATE                   DEFAULT NULL COMMENT '离职日期',
    education         VARCHAR(20)            DEFAULT NULL COMMENT '学历',
    school            VARCHAR(80)            DEFAULT NULL COMMENT '毕业院校',
    major             VARCHAR(80)            DEFAULT NULL COMMENT '专业',
    emergency_contact VARCHAR(40)            DEFAULT NULL COMMENT '紧急联系人',
    emergency_phone   VARCHAR(20)            DEFAULT NULL COMMENT '紧急联系电话',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_emp_no (emp_no),
    KEY idx_employee_status (status),
    KEY idx_employee_dept (department),
    CONSTRAINT fk_employee_grade FOREIGN KEY (grade_code) REFERENCES salary_grade (grade_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '员工档案';

CREATE TABLE IF NOT EXISTS user_account (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    employee_id          BIGINT       NOT NULL COMMENT '员工ID',
    password_hash        VARCHAR(160) NOT NULL COMMENT 'PBKDF2 密码哈希',
    role                 VARCHAR(12)  NOT NULL COMMENT 'MANAGER/HR/EMPLOYEE',
    must_change_password TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '首次登录需改密',
    last_login_at        DATETIME              DEFAULT NULL,
    created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_account_employee (employee_id),
    CONSTRAINT fk_account_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '登录账号';

CREATE TABLE IF NOT EXISTS employee_salary (
    employee_id BIGINT         NOT NULL COMMENT '员工ID',
    allowance   DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '岗位津贴',
    bonus_ratio DECIMAL(5, 4)  NOT NULL DEFAULT 0.2000 COMMENT '绩效奖金比例',
    updated_by  BIGINT                  DEFAULT NULL COMMENT '最近操作人',
    updated_at  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (employee_id),
    CONSTRAINT fk_salary_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '员工薪酬参数';

CREATE TABLE IF NOT EXISTS work_experience (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    employee_id BIGINT       NOT NULL,
    company     VARCHAR(80)  NOT NULL COMMENT '公司',
    position    VARCHAR(40)  NOT NULL COMMENT '职位',
    start_date  DATE         NOT NULL,
    end_date    DATE                  DEFAULT NULL,
    description VARCHAR(300)          DEFAULT NULL COMMENT '工作内容',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_we_employee (employee_id),
    CONSTRAINT fk_we_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '曾就职经历';

CREATE TABLE IF NOT EXISTS performance_record (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    employee_id    BIGINT        NOT NULL,
    period         CHAR(7)       NOT NULL COMMENT 'yyyy-MM',
    score          DECIMAL(5, 2) NOT NULL COMMENT '业绩分 0-100',
    self_desc      VARCHAR(500)           DEFAULT NULL COMMENT '自评说明',
    status         VARCHAR(10)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    reviewer_id    BIGINT                 DEFAULT NULL,
    review_comment VARCHAR(200)           DEFAULT NULL,
    submitted_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at    DATETIME               DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_perf_employee_period (employee_id, period),
    KEY idx_perf_status (status),
    CONSTRAINT fk_perf_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '业绩记录';

CREATE TABLE IF NOT EXISTS red_packet (
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    employee_id BIGINT         NOT NULL COMMENT '收件人',
    amount      DECIMAL(12, 2) NOT NULL COMMENT '金额',
    message     VARCHAR(200)            DEFAULT NULL COMMENT '附言（仅收件人可见）',
    sender_id   BIGINT         NOT NULL COMMENT '发放人',
    read_flag   TINYINT(1)     NOT NULL DEFAULT 0,
    created_at  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_rp_employee (employee_id),
    CONSTRAINT fk_rp_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '私发红包';

CREATE TABLE IF NOT EXISTS salary_monthly (
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    employee_id       BIGINT         NOT NULL,
    month             CHAR(7)        NOT NULL COMMENT 'yyyy-MM',
    base_salary       DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    allowance         DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    performance_bonus DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    red_packet        DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total             DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    created_at        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_salary_employee_month (employee_id, month),
    CONSTRAINT fk_sm_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '月度薪资快照';

CREATE TABLE IF NOT EXISTS salary_adjustment (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    employee_id BIGINT       NOT NULL,
    type        VARCHAR(20)  NOT NULL COMMENT 'GRADE/ALLOWANCE/BONUS_RATIO',
    old_value   VARCHAR(40)           DEFAULT NULL,
    new_value   VARCHAR(40)           DEFAULT NULL,
    reason      VARCHAR(200) NOT NULL,
    operator_id BIGINT       NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_sa_employee (employee_id),
    CONSTRAINT fk_sa_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '薪酬调整流水';
