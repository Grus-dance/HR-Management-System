"""生成 src/main/resources/db/data.sql 演示数据。

数据口径与设计文档一致：
  应发 = 基本工资 + 岗位津贴 + 基本工资×奖金比例×有效业绩分/100 + 当月红包
历史月份（含当前月之前的 5 个月）写入 salary_monthly 快照；当月由业务层实时计算。
"""
from __future__ import annotations

import base64
import hashlib
from datetime import date
from decimal import Decimal, ROUND_HALF_UP
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src" / "main" / "resources" / "db" / "data.sql"

GRADES = [
    ("G1", "助理", 6000, "应届/实习转正", 1),
    ("G2", "专员", 8000, "独立完成常规工作", 2),
    ("G3", "高级专员", 10000, "熟练承担专业工作", 3),
    ("G4", "主管", 13000, "带领小团队", 4),
    ("G5", "高级主管", 16000, "跨团队协作", 5),
    ("G6", "经理", 20000, "部门级管理", 6),
    ("G7", "高级经理", 25000, "多部门协同", 7),
    ("G8", "总监", 32000, "公司级管理", 8),
]
BASE = {g[0]: g[2] for g in GRADES}

# id, emp_no, name, gender, dept, position, grade, hire_date, status, resign_date
EMPLOYEES = [
    (1, "M0001", "张伟", "M", "总经办", "总经理", "G8", "2015-03-01", "ACTIVE", None),
    (2, "H0001", "李娜", "F", "人力资源部", "人力资源经理", "G6", "2017-06-15", "ACTIVE", None),
    (3, "H0002", "王强", "M", "人力资源部", "人力资源专员", "G4", "2021-09-01", "ACTIVE", None),
    (4, "X1001", "陈晨", "F", "技术部", "后端工程师", "G5", "2020-04-12", "ACTIVE", None),
    (5, "X1002", "刘洋", "M", "技术部", "前端工程师", "G4", "2021-07-05", "ACTIVE", None),
    (6, "X1003", "赵敏", "F", "技术部", "测试工程师", "G3", "2022-02-21", "ACTIVE", None),
    (7, "X1004", "孙浩", "M", "技术部", "架构师", "G6", "2017-11-08", "ACTIVE", None),
    (8, "X1005", "周静", "F", "产品部", "产品经理", "G5", "2019-08-19", "ACTIVE", None),
    (9, "X1006", "吴磊", "M", "产品部", "产品专员", "G3", "2022-06-01", "ACTIVE", None),
    (10, "X1007", "郑爽", "F", "市场部", "市场经理", "G5", "2020-01-13", "ACTIVE", None),
    (11, "X1008", "冯婷", "F", "市场部", "市场专员", "G3", "2023-03-06", "ACTIVE", None),
    (12, "X1009", "何伟", "M", "财务部", "财务主管", "G4", "2019-10-21", "ACTIVE", None),
    (13, "X1010", "许晴", "F", "财务部", "会计", "G3", "2021-12-01", "ACTIVE", None),
    (14, "X1011", "曹阳", "M", "技术部", "运维工程师", "G4", "2020-09-14", "ACTIVE", None),
    (15, "X1012", "邓超", "M", "市场部", "市场专员", "G3", "2022-11-07", "RESIGNED", "2026-06-30"),
]

ALLOWANCE = {1: 8000, 2: 3500, 3: 1200, 4: 3000, 5: 2200, 6: 1500, 7: 4000,
             8: 2800, 9: 1200, 10: 2600, 11: 1300, 12: 2000, 13: 1200, 14: 1800, 15: 1000}
RATIO = {4: Decimal("0.2200"), 7: Decimal("0.2500")}
ROLE = {1: "MANAGER", 2: "HR", 3: "HR"}
MUST_CHANGE = {4: 1}

BIRTH = {1: "1982-05-11", 2: "1987-09-23", 3: "1993-02-17", 4: "1994-11-02", 5: "1995-06-18",
         6: "1998-01-09", 7: "1985-12-30", 8: "1991-07-07", 9: "1999-04-25", 10: "1992-10-16",
         11: "2000-03-03", 12: "1990-08-21", 13: "1996-05-14", 14: "1993-12-05", 15: "1997-09-28"}
EDUCATION = {1: ("硕士", "吉林大学", "工商管理"), 2: ("硕士", "东北师范大学", "人力资源管理"),
             3: ("本科", "吉林大学", "行政管理"), 4: ("硕士", "哈尔滨工业大学", "计算机科学与技术"),
             5: ("本科", "吉林大学", "软件工程"), 6: ("本科", "长春理工大学", "计算机科学与技术"),
             7: ("硕士", "浙江大学", "软件工程"), 8: ("硕士", "吉林大学", "产品设计"),
             9: ("本科", "东北大学", "工业设计"), 10: ("本科", "吉林财经大学", "市场营销"),
             11: ("本科", "吉林大学", "广告学"), 12: ("本科", "东北财经大学", "会计学"),
             13: ("本科", "吉林财经大学", "会计学"), 14: ("本科", "长春工业大学", "网络工程"),
             15: ("本科", "吉林师范大学", "市场营销")}
ID_CARD = {1: "220102198205111230", 2: "220104198709233622", 3: "220102199302174513",
           4: "220103199411024228", 5: "220104199506185617", 6: "220105199801092226",
           7: "220102198512301514", 8: "220104199107074428", 9: "220105199904253315",
           10: "220102199210164622", 11: "220103200003034826", 12: "220104199008215511",
           13: "220105199605144420", 14: "220103199312055217", 15: "220102199709283314"}


def money(value) -> Decimal:
    return Decimal(value).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)


def salt_for(emp_no: str) -> bytes:
    return hashlib.sha256(("hrm-salt-" + emp_no).encode()).digest()[:16]


def hash_password(password: str, emp_no: str) -> str:
    salt = salt_for(emp_no)
    digest = hashlib.pbkdf2_hmac("sha256", password.encode(), salt, 120000, 32)
    return "pbkdf2$120000${}${}".format(
        base64.b64encode(salt).decode(), base64.b64encode(digest).decode())


def months_back(today: date, count: int) -> list[str]:
    result = []
    year, month = today.year, today.month
    for _ in range(count):
        result.append(f"{year:04d}-{month:02d}")
        month -= 1
        if month == 0:
            year -= 1
            month = 12
    return list(reversed(result))


def sql_str(value) -> str:
    if value is None:
        return "NULL"
    return "'" + str(value).replace("\\", "\\\\").replace("'", "''") + "'"


def score_for(emp_id: int, month_index: int) -> Decimal:
    base = 78 + (emp_id * 7 + month_index * 11) % 17  # 78-94
    return Decimal(base)


def main() -> None:
    today = date(2026, 10, 23)
    months = months_back(today, 6)          # 2026-05 .. 2026-10
    history = months[:-1]                   # 2026-05 .. 2026-09
    current = months[-1]                    # 2026-10
    lines: list[str] = []
    add = lines.append

    add("-- 自动生成：X公司人事管理系统演示数据（全部为虚构信息）")
    add("-- 生成日期：{}".format(today.isoformat()))
    add("SET NAMES utf8mb4;")
    add("")

    add("-- 薪酬等级")
    for code, name, base, desc, sort_no in GRADES:
        add("INSERT IGNORE INTO salary_grade (grade_code, grade_name, base_salary, description, sort_no) "
            f"VALUES ({sql_str(code)}, {sql_str(name)}, {base}.00, {sql_str(desc)}, {sort_no});")
    add("")

    add("-- 员工档案")
    for eid, emp_no, name, gender, dept, pos, grade, hire, status, resign in EMPLOYEES:
        edu, school, major = EDUCATION[eid]
        phone = f"138{10000000 + eid * 137:08d}"
        email = f"{emp_no.lower()}@xcompany.com"
        address = f"吉林省长春市朝阳区示例路 {eid} 号"
        add("INSERT IGNORE INTO employee (id, emp_no, name, gender, birth_date, id_card, phone, email, address, "
            "department, position, grade_code, hire_date, status, resign_date, education, school, major, "
            "emergency_contact, emergency_phone) VALUES ("
            f"{eid}, {sql_str(emp_no)}, {sql_str(name)}, {sql_str(gender)}, {sql_str(BIRTH[eid])}, "
            f"{sql_str(ID_CARD[eid])}, {sql_str(phone)}, {sql_str(email)}, {sql_str(address)}, "
            f"{sql_str(dept)}, {sql_str(pos)}, {sql_str(grade)}, {sql_str(hire)}, {sql_str(status)}, "
            f"{sql_str(resign)}, {sql_str(edu)}, {sql_str(school)}, {sql_str(major)}, "
            f"{sql_str('家人-' + name)}, {sql_str('139' + str(20000000 + eid * 211).zfill(8))});")
    add("")

    add("-- 登录账号（初始密码均为 123456）")
    for eid, emp_no, name, gender, dept, pos, grade, hire, status, resign in EMPLOYEES:
        add("INSERT IGNORE INTO user_account (employee_id, password_hash, role, must_change_password) VALUES ("
            f"{eid}, {sql_str(hash_password('123456', emp_no))}, {sql_str(ROLE.get(eid, 'EMPLOYEE'))}, "
            f"{MUST_CHANGE.get(eid, 0)});")
    add("")

    add("-- 薪酬参数")
    for eid, *_ in EMPLOYEES:
        add("INSERT IGNORE INTO employee_salary (employee_id, allowance, bonus_ratio) VALUES ("
            f"{eid}, {ALLOWANCE[eid]}.00, {RATIO.get(eid, Decimal('0.2000'))});")
    add("")

    add("-- 曾就职经历")
    companies = ["长春信息技术有限公司", "吉林数字科技有限公司", "东北软件园股份有限公司",
                 "长春智能装备有限公司", "吉林省云服务有限公司"]
    we_id = 1
    for eid, emp_no, name, gender, dept, pos, grade, hire, status, resign in EMPLOYEES:
        count = 2 if eid % 2 == 0 else 1
        for idx in range(count):
            company = companies[(eid + idx) % len(companies)]
            end_year = int(hire[:4]) - 1 - idx
            add("INSERT IGNORE INTO work_experience (id, employee_id, company, position, start_date, end_date, description) "
                f"VALUES ({we_id}, {eid}, {sql_str(company)}, {sql_str(pos)}, "
                f"{sql_str(f'{end_year - 2}-07-01')}, {sql_str(f'{end_year}-06-30')}, "
                f"{sql_str('负责' + pos + '相关工作，参与核心项目交付与团队协作。')});")
            we_id += 1
    add("")

    add("-- 薪酬调整流水")
    adjustments = [
        (4, "GRADE", "G4", "G5", "年度晋升，晋升为高级主管", 1, "2026-01-15 09:30:00"),
        (5, "ALLOWANCE", "1500.00", "2000.00", "技能津贴上调", 2, "2026-03-01 10:00:00"),
        (7, "GRADE", "G5", "G6", "技术架构负责人任命", 1, "2025-07-01 09:00:00"),
        (10, "ALLOWANCE", "1000.00", "1800.00", "市场岗位津贴调整", 3, "2026-04-10 14:20:00"),
    ]
    for adj_id, (emp_id, typ, old, new, reason, operator, at) in enumerate(adjustments, start=1):
        add("INSERT IGNORE INTO salary_adjustment (id, employee_id, type, old_value, new_value, reason, operator_id, created_at) "
            f"VALUES ({adj_id}, {emp_id}, {sql_str(typ)}, {sql_str(old)}, {sql_str(new)}, {sql_str(reason)}, {operator}, {sql_str(at)});")
    add("")

    add("-- 业绩记录")
    pending = {4, 5, 8, 10}
    rejected = {9}
    for eid, emp_no, name, gender, dept, pos, grade, hire, status, resign in EMPLOYEES:
        if status == "RESIGNED":
            continue
        for index, month in enumerate(months):
            if month == current and eid in pending:
                st, comment, reviewer, reviewed = "PENDING", None, None, None
            elif month == current and eid in rejected:
                st, comment, reviewer, reviewed = "REJECTED", "自评材料不完整，请补充数据后重新提交", 2, "2026-10-20 16:40:00"
            else:
                st = "APPROVED"
                comment = "数据完整，确认有效"
                reviewer = 2 if eid != 2 else 3
                reviewed = f"{month}-21 15:00:00"
            add("INSERT IGNORE INTO performance_record (employee_id, period, score, self_desc, status, reviewer_id, "
                "review_comment, submitted_at, reviewed_at) VALUES ("
                f"{eid}, {sql_str(month)}, {score_for(eid, index)}, "
                f"{sql_str('本月完成既定目标，参与团队协作与知识分享。')}, {sql_str(st)}, "
                f"{sql_str(reviewer) if reviewer else 'NULL'}, {sql_str(comment) if comment else 'NULL'}, "
                f"{sql_str(month + '-20 10:30:00')}, {sql_str(reviewed) if reviewed else 'NULL'});")
    add("")

    add("-- 私发红包")
    packets = [
        (4, 2000.00, "项目攻坚奖励，辛苦了！", 1, 1, "2026-08-15 18:00:00"),
        (8, 1500.00, "产品发布贡献奖", 1, 1, "2026-09-10 17:30:00"),
        (6, 800.00, "测试质量提升奖励", 1, 1, "2026-07-20 16:00:00"),
        (4, 1000.00, "季度之星，继续保持！", 1, 0, "2026-10-08 09:20:00"),
    ]
    for rp_id, (emp_id, amount, message, sender, read_flag, at) in enumerate(packets, start=1):
        add("INSERT IGNORE INTO red_packet (id, employee_id, amount, message, sender_id, read_flag, created_at) "
            f"VALUES ({rp_id}, {emp_id}, {amount}, {sql_str(message)}, {sender}, {read_flag}, {sql_str(at)});")
    add("")

    add("-- 历史月度薪资快照（当月实时计算，不落库）")
    for eid, emp_no, name, gender, dept, pos, grade, hire, status, resign in EMPLOYEES:
        for index, month in enumerate(months):
            if month == current:
                continue
            if status == "RESIGNED" and month > "2026-06":
                continue
            base = Decimal(BASE[grade])
            allowance = Decimal(ALLOWANCE[eid])
            ratio = RATIO.get(eid, Decimal("0.2000"))
            score = score_for(eid, index)
            bonus = money(base * ratio * score / Decimal(100))
            red = Decimal("0.00")
            for p in packets:
                if p[0] == eid and p[5][:7] == month:
                    red += Decimal(str(p[1]))
            total = money(base + allowance + bonus + red)
            add("INSERT IGNORE INTO salary_monthly (employee_id, month, base_salary, allowance, performance_bonus, "
                f"red_packet, total) VALUES ({eid}, {sql_str(month)}, {money(base)}, {money(allowance)}, "
                f"{bonus}, {money(red)}, {total});")
    add("")

    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text("\n".join(lines), encoding="utf-8")
    print(f"written {OUT} ({len(lines)} lines), months={months}")


if __name__ == "__main__":
    main()
