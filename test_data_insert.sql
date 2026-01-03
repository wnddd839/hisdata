-- ============================================
-- HIS系统测试数据填充脚本
-- 创建日期: 2026-01-02
-- 说明: 填充各个表的测试数据，包括药品、检查项目、医生、患者等
-- ============================================

USE his_db;

-- ============================================
-- 1. 清空现有测试数据 (可选)
-- ============================================
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE finance;
-- TRUNCATE TABLE prescriptions;
-- TRUNCATE TABLE medical_records;
-- TRUNCATE TABLE tests;
-- TRUNCATE TABLE appointments;
-- TRUNCATE TABLE pharmacy_inventory;
-- TRUNCATE TABLE login_log;
-- TRUNCATE TABLE doctors;
-- TRUNCATE TABLE ai_patient;
-- TRUNCATE TABLE user_accounts;
-- TRUNCATE TABLE discounts;
-- SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 2. 用户账号数据 (user_accounts)
-- ============================================
INSERT INTO user_accounts (username, password, role, created_at, updated_at) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ROLE_ADMIN', NOW(), NOW()), -- 密码: admin123
('doctor1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ROLE_DOCTOR', NOW(), NOW()), -- 密码: admin123
('doctor2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ROLE_DOCTOR', NOW(), NOW()), -- 密码: admin123
('doctor3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ROLE_DOCTOR', NOW(), NOW()), -- 密码: admin123
('doctor4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ROLE_DOCTOR', NOW(), NOW()), -- 密码: admin123
('doctor5', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ROLE_DOCTOR', NOW(), NOW()), -- 密码: admin123
('patient1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ROLE_PATIENT', NOW(), NOW()), -- 密码: admin123
('patient2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ROLE_PATIENT', NOW(), NOW()), -- 密码: admin123
('patient3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ROLE_PATIENT', NOW(), NOW()), -- 密码: admin123
('patient4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ROLE_PATIENT', NOW(), NOW()), -- 密码: admin123
('patient5', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ROLE_PATIENT', NOW(), NOW()); -- 密码: admin123

-- ============================================
-- 3. 医生数据 (doctors)
-- ============================================
INSERT INTO doctors (name, title, department, phone, created_at, updated_at) VALUES
('张伟', '主任医师', '内科', '13800001001', NOW(), NOW()),
('李娜', '副主任医师', '外科', '13800001002', NOW(), NOW()),
('王强', '主治医师', '儿科', '13800001003', NOW(), NOW()),
('刘芳', '主任医师', '妇科', '13800001004', NOW(), NOW()),
('陈明', '副主任医师', '眼科', '13800001005', NOW(), NOW()),
('杨洋', '主治医师', '耳鼻喉科', '13800001006', NOW(), NOW()),
('赵丽', '主任医师', '骨科', '13800001007', NOW(), NOW()),
('孙涛', '副主任医师', '神经内科', '13800001008', NOW(), NOW()),
('周杰', '主治医师', '心内科', '13800001009', NOW(), NOW()),
('吴敏', '主任医师', '呼吸内科', '13800001010', NOW(), NOW());

-- ============================================
-- 4. 患者数据 (ai_patient)
-- ============================================
INSERT INTO ai_patient (name, gender, age, phone, address, user_id, card_number, id_card, medical_history, allergy, is_deleted, created_at, updated_at) VALUES
('张小明', 1, 28, '13900002001', '北京市朝阳区', 7, 'P202601010001', '110101199601011234', '高血压病史', '青霉素', 0, NOW(), NOW()),
('李小红', 2, 35, '13900002002', '北京市海淀区', 8, 'P202601010002', '110101198902022345', '糖尿病史', '无', 0, NOW(), NOW()),
('王大力', 1, 42, '13900002003', '北京市西城区', 9, 'P202601010003', '110101198203033456', '冠心病史', '阿司匹林', 0, NOW(), NOW()),
('刘美丽', 2, 25, '13900002004', '北京市东城区', 10, 'P202601010004', '110101199904044567', '无', '花粉过敏', 0, NOW(), NOW()),
('陈建国', 1, 55, '13900002005', '北京市丰台区', 11, 'P202601010005', '110101196905055678', '高血压、糖尿病', '磺胺类药物', 0, NOW(), NOW());

-- ============================================
-- 5. 药品库存数据 (pharmacy_inventory)
-- ============================================
INSERT INTO pharmacy_inventory (medicine_name, specification, manufacturer, stock_quantity, unit_price, production_date, expiry_date, category, created_at, updated_at) VALUES
-- 抗生素类
('阿莫西林胶囊', '0.25g*24粒', '修正药业', 500, 18.50, '2025-01-01', '2027-01-01', '抗生素', NOW(), NOW()),
('头孢拉定胶囊', '0.25g*24粒', '白云山药业', 450, 25.80, '2025-02-01', '2027-02-01', '抗生素', NOW(), NOW()),
('阿奇霉素片', '0.25g*6片', '辉瑞制药', 300, 42.00, '2025-03-01', '2027-03-01', '抗生素', NOW(), NOW()),
('左氧氟沙星片', '0.5g*6片', '拜耳医药', 380, 35.60, '2025-01-15', '2027-01-15', '抗生素', NOW(), NOW()),

-- 退烧止痛类
('布洛芬缓释胶囊', '0.3g*20粒', '芬必得', 600, 28.90, '2025-04-01', '2027-04-01', '解热镇痛', NOW(), NOW()),
('对乙酰氨基酚片', '0.5g*10片', '泰诺', 800, 12.50, '2025-05-01', '2027-05-01', '解热镇痛', NOW(), NOW()),
('双氯芬酸钠肠溶片', '25mg*30片', '迪沙药业', 400, 15.80, '2025-02-15', '2027-02-15', '解热镇痛', NOW(), NOW()),

-- 消化系统类
('奥美拉唑肠溶胶囊', '20mg*14粒', '阿斯利康', 550, 45.00, '2025-03-15', '2027-03-15', '消化系统', NOW(), NOW()),
('多潘立酮片', '10mg*30片', '吗丁啉', 700, 22.50, '2025-06-01', '2027-06-01', '消化系统', NOW(), NOW()),
('蒙脱石散', '3g*10袋', '思密达', 900, 18.00, '2025-01-20', '2027-01-20', '消化系统', NOW(), NOW()),

-- 心血管系统类
('阿托伐他汀钙片', '20mg*7片', '立普妥', 350, 68.00, '2025-04-15', '2027-04-15', '心血管', NOW(), NOW()),
('硝苯地平控释片', '30mg*7片', '拜新同', 420, 55.00, '2025-05-15', '2027-05-15', '心血管', NOW(), NOW()),
('阿司匹林肠溶片', '100mg*30片', '拜耳', 1000, 15.20, '2025-07-01', '2027-07-01', '心血管', NOW(), NOW()),
('酒石酸美托洛尔片', '25mg*20片', '倍他乐克', 580, 32.50, '2025-06-15', '2027-06-15', '心血管', NOW(), NOW()),

-- 呼吸系统类
('盐酸氨溴索口服溶液', '100ml', '沐舒坦', 650, 28.80, '2025-08-01', '2027-08-01', '呼吸系统', NOW(), NOW()),
('复方甘草口服液', '100ml', '葵花药业', 500, 12.50, '2025-03-20', '2027-03-20', '呼吸系统', NOW(), NOW()),
('氯雷他定片', '10mg*6片', '开瑞坦', 720, 38.00, '2025-07-15', '2027-07-15', '抗过敏', NOW(), NOW()),

-- 维生素及营养类
('维生素C片', '100mg*100片', '养生堂', 1200, 8.50, '2025-09-01', '2027-09-01', '维生素', NOW(), NOW()),
('维生素B族片', '100片', '汤臣倍健', 850, 45.00, '2025-08-15', '2027-08-15', '维生素', NOW(), NOW()),
('碳酸钙D3片', '60片', '钙尔奇', 680, 52.00, '2025-10-01', '2027-10-01', '营养', NOW(), NOW()),

-- 中成药
('感冒灵颗粒', '10g*9袋', '999感冒灵', 1100, 15.00, '2025-11-01', '2027-11-01', '中成药', NOW(), NOW()),
('板蓝根颗粒', '10g*20袋', '白云山', 1300, 18.50, '2025-09-15', '2027-09-15', '中成药', NOW(), NOW()),
('六味地黄丸', '360丸', '同仁堂', 480, 38.00, '2025-12-01', '2027-12-01', '中成药', NOW(), NOW());

-- ============================================
-- 6. 挂号数据 (appointments)
-- ============================================
INSERT INTO appointments (pid, patient_id, patient_name, doctor_id, doctor_name, department, registration_date, registration_time, registration_fee, serial_number, status, created_at, updated_at) VALUES
-- 今日挂号 (2026-01-02)
(1, 1, '张小明', 1, '张伟', '内科', CURDATE(), '08:30:00', 50.00, 1, 1, NOW(), NOW()),
(1, 1, '张小明', 3, '王强', '儿科', CURDATE(), '09:00:00', 50.00, 2, 3, NOW(), NOW()),
(2, 2, '李小红', 2, '李娜', '外科', CURDATE(), '09:30:00', 50.00, 3, 2, NOW(), NOW()),
(3, 3, '王大力', 1, '张伟', '内科', CURDATE(), '10:00:00', 50.00, 4, 1, NOW(), NOW()),
(4, 4, '刘美丽', 4, '刘芳', '妇科', CURDATE(), '10:30:00', 50.00, 5, 1, NOW(), NOW()),
(5, 5, '陈建国', 8, '孙涛', '神经内科', CURDATE(), '11:00:00', 50.00, 6, 1, NOW(), NOW()),

-- 昨日挂号 (2026-01-01)
(1, 1, '张小明', 1, '张伟', '内科', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '14:00:00', 50.00, 15, 3, NOW(), NOW()),
(2, 2, '李小红', 2, '李娜', '外科', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '15:00:00', 50.00, 16, 3, NOW(), NOW()),
(3, 3, '王大力', 7, '赵丽', '骨科', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '16:00:00', 50.00, 17, 0, NOW(), NOW()), -- 已取消
(5, 5, '陈建国', 9, '周杰', '心内科', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '16:30:00', 50.00, 18, 3, NOW(), NOW());

-- ============================================
-- 7. 病历数据 (medical_records)
-- ============================================
INSERT INTO medical_records (appointment_id, pid, doctor_id, chief_complaint, history_present, diagnosis, treatment_plan, created_at, updated_at) VALUES
(1, 1, 1, '头痛、头晕3天', '患者3天前无明显诱因出现头痛、头晕，伴有恶心，无呕吐。既往有高血压病史。', '高血压病', '1. 低盐饮食\n2. 降压治疗：硝苯地平控释片 30mg 每日1次\n3. 监测血压\n4. 1周后复查', NOW(), NOW()),

(3, 2, 2, '右下腹痛4小时', '患者4小时前突发右下腹痛，呈持续性疼痛，伴恶心、呕吐1次。无发热。', '急性阑尾炎', '1. 禁食水\n2. 抗感染治疗：头孢拉定 0.5g 每日3次\n3. 完善相关检查\n4. 择期手术', NOW(), NOW()),

(4, 3, 1, '咳嗽、咳痰1周', '患者1周前受凉后出现咳嗽、咳少量白色黏痰，无发热、胸痛。', '急性支气管炎', '1. 注意休息，多饮水\n2. 止咳化痰：盐酸氨溴索 10ml 每日3次\n3. 抗感染：阿莫西林 0.5g 每日3次\n4. 3天后复查', NOW(), NOW()),

(7, 1, 1, '胸闷、气短2天', '患者2天前活动后出现胸闷、气短，休息后可缓解。', '冠心病、心绞痛', '1. 低脂饮食\n2. 扩冠治疗：硝酸甘油 0.5mg 舌下含服 必要时\n3. 抗血小板：阿司匹林 100mg 每日1次\n4. 建议冠脉造影检查', NOW(), NOW()),

(8, 2, 2, '外伤后右踝肿痛', '患者2小时前下楼梯时扭伤右踝，当即疼痛，活动受限。', '右踝关节扭伤', '1. 制动休息\n2. 局部冷敷\n3. 消肿止痛：布洛芬 0.3g 每日2次\n4. 3天后复查X线', NOW(), NOW()),

(10, 5, 8, '右侧肢体麻木', '患者1周前出现右侧肢体麻木，持物不稳。既往有高血压、糖尿病病史。', '脑梗塞', '1. 控制血压血糖\n2. 抗血小板：阿司匹林 100mg 每日1次\n3. 改善循环：银杏叶提取物\n4. 建议头颅MRI检查', NOW(), NOW());

-- ============================================
-- 8. 处方数据 (prescriptions)
-- ============================================
INSERT INTO prescriptions (appointment_id, pid, doctor_id, medicine_id, medicine_name, dosage, frequency, quantity, price, status, created_at, updated_at) VALUES
-- 挂号1的处方（高血压）
(1, 1, 1, 12, '硝苯地平控释片', '30mg', '每日1次', 7, 55.00, 2, NOW(), NOW()),
(1, 1, 1, 13, '阿司匹林肠溶片', '100mg', '每日1次', 30, 15.20, 2, NOW(), NOW()),

-- 挂号3的处方（急性阑尾炎）
(3, 2, 2, 2, '头孢拉定胶囊', '0.5g', '每日3次', 24, 25.80, 2, NOW(), NOW()),

-- 挂号4的处方（急性支气管炎）
(4, 3, 1, 16, '盐酸氨溴索口服溶液', '10ml', '每日3次', 3, 28.80, 2, NOW(), NOW()),
(4, 3, 1, 1, '阿莫西林胶囊', '0.5g', '每日3次', 21, 18.50, 2, NOW(), NOW()),

-- 挂号7的处方（冠心病）
(7, 1, 1, 13, '阿司匹林肠溶片', '100mg', '每日1次', 30, 15.20, 2, NOW(), NOW()),
(7, 1, 1, 14, '酒石酸美托洛尔片', '25mg', '每日2次', 14, 32.50, 2, NOW(), NOW()),

-- 挂号8的处方（踝关节扭伤）
(8, 2, 2, 5, '布洛芬缓释胶囊', '0.3g', '每日2次', 10, 28.90, 2, NOW(), NOW()),

-- 挂号10的处方（脑梗塞）
(10, 5, 8, 13, '阿司匹林肠溶片', '100mg', '每日1次', 30, 15.20, 2, NOW(), NOW());

-- ============================================
-- 9. 检查检验数据 (tests)
-- ============================================
INSERT INTO tests (appointment_id, pid, doctor_id, test_name, test_type, fee, status, result, created_at, updated_at) VALUES
-- 挂号1的检查（高血压）
(1, 1, 1, '血压监测', '其他', 10.00, 3, '血压165/105mmHg，偏高', NOW(), NOW()),
(1, 1, 1, '心电图', '心电图', 30.00, 3, '窦性心律，正常心电图', NOW(), NOW()),

-- 挂号3的检查（急性阑尾炎）
(3, 2, 2, '血常规', '化验', 25.00, 3, 'WBC 12.5×10^9/L，N 85%，提示感染', NOW(), NOW()),
(3, 2, 2, '腹部B超', 'B超', 80.00, 3, '阑尾区可见肿大阑尾，壁厚，周围渗出', NOW(), NOW()),

-- 挂号4的检查（急性支气管炎）
(4, 3, 1, '胸部X线', 'X线', 60.00, 3, '双肺纹理增多，未见明显实质性病变', NOW(), NOW()),

-- 挂号7的检查（冠心病）
(7, 1, 1, '心电图', '心电图', 30.00, 3, '窦性心律，II、III、aVF导联ST段压低0.05mV', NOW(), NOW()),
(7, 1, 1, '心脏彩超', '彩超', 150.00, 1, NULL, NOW(), NOW()),

-- 挂号8的检查（踝关节扭伤）
(8, 2, 2, '右踝关节X线', 'X线', 80.00, 3, '右踝关节未见明显骨折，关节间隙正常', NOW(), NOW()),

-- 挂号10的检查（脑梗塞）
(10, 5, 8, '头颅CT', 'CT', 200.00, 3, '左侧基底节区低密度灶，提示脑梗塞', NOW(), NOW()),
(10, 5, 8, '头颅MRI', 'MRI', 500.00, 1, NULL, NOW(), NOW());

-- ============================================
-- 10. 财务账单数据 (finance)
-- ============================================
INSERT INTO finance (appointment_id, pid, registration_fee, test_fee, medicine_fee, total_amount, discount_amount, final_amount, payment_status, payment_time, created_at, updated_at) VALUES
-- 挂号1的账单（高血压）
(1, 1, 50.00, 40.00, 70.20, 160.20, 0.00, 160.20, 1, NOW(), NOW(), NOW()),

-- 挂号2的账单（儿科）
(2, 1, 50.00, 0.00, 0.00, 50.00, 0.00, 50.00, 1, NOW(), NOW(), NOW()),

-- 挂号3的账单（急性阑尾炎）
(3, 2, 50.00, 105.00, 25.80, 180.80, 0.00, 180.80, 1, NOW(), NOW(), NOW()),

-- 挂号4的账单（急性支气管炎）
(4, 3, 50.00, 60.00, 47.30, 157.30, 0.00, 157.30, 1, NOW(), NOW(), NOW()),

-- 挂号5的账单（妇科）
(5, 4, 50.00, 0.00, 0.00, 50.00, 0.00, 50.00, 0, NULL, NOW(), NOW()),

-- 挂号6的账单（神经内科）
(6, 5, 50.00, 0.00, 0.00, 50.00, 0.00, 50.00, 0, NULL, NOW(), NOW()),

-- 挂号7的账单（冠心病）
(7, 1, 50.00, 180.00, 47.70, 277.70, 0.00, 277.70, 1, NOW(), NOW(), NOW()),

-- 挂号8的账单（踝关节扭伤）
(8, 2, 50.00, 80.00, 28.90, 158.90, 0.00, 158.90, 1, NOW(), NOW(), NOW()),

-- 挂号10的账单（脑梗塞）
(10, 5, 50.00, 700.00, 15.20, 765.20, 0.00, 765.20, 1, NOW(), NOW(), NOW());

-- ============================================
-- 11. 优惠码数据 (discounts)
-- ============================================
INSERT INTO discounts (code, discount_type, discount_value, min_amount, max_discount, status, valid_from, valid_until, created_at, updated_at) VALUES
('NEW2026', 'PERCENTAGE', 10.00, 100.00, 50.00, 1, '2026-01-01', '2026-12-31', NOW(), NOW()),
('VIP888', 'PERCENTAGE', 15.00, 200.00, 100.00, 1, '2026-01-01', '2026-06-30', NOW(), NOW()),
('FLAT50', 'FIXED', 50.00, 300.00, 50.00, 1, '2026-01-01', '2026-03-31', NOW(), NOW()),
('SPRING2026', 'PERCENTAGE', 20.00, 500.00, 150.00, 1, '2026-01-01', '2026-03-31', NOW(), NOW());

-- ============================================
-- 12. 登录日志数据 (login_log)
-- ============================================
INSERT INTO login_log (user_id, username, login_time, ip_address, user_agent, status) VALUES
(1, 'admin', NOW(), '127.0.0.1', 'Mozilla/5.0', 1),
(2, 'doctor1', NOW(), '127.0.0.1', 'Mozilla/5.0', 1),
(7, 'patient1', NOW(), '127.0.0.1', 'Mozilla/5.0', 1);

-- ============================================
-- 数据统计
-- ============================================
SELECT '数据填充完成！' AS message;
SELECT CONCAT('用户账号: ', COUNT(*), ' 条') AS summary FROM user_accounts
UNION ALL
SELECT CONCAT('医生: ', COUNT(*), ' 条') FROM doctors
UNION ALL
SELECT CONCAT('患者: ', COUNT(*), ' 条') FROM ai_patient
UNION ALL
SELECT CONCAT('药品: ', COUNT(*), ' 条') FROM pharmacy_inventory
UNION ALL
SELECT CONCAT('挂号: ', COUNT(*), ' 条') FROM appointments
UNION ALL
SELECT CONCAT('病历: ', COUNT(*), ' 条') FROM medical_records
UNION ALL
SELECT CONCAT('处方: ', COUNT(*), ' 条') FROM prescriptions
UNION ALL
SELECT CONCAT('检查: ', COUNT(*), ' 条') FROM tests
UNION ALL
SELECT CONCAT('财务: ', COUNT(*), ' 条') FROM finance
UNION ALL
SELECT CONCAT('优惠码: ', COUNT(*), ' 条') FROM discounts;

-- ============================================
-- 测试账号信息
-- ============================================
SELECT '====================' AS '测试账号信息';
SELECT CONCAT('角色: ', role) AS '账号类型', username AS '用户名', 'admin123' AS '密码'
FROM user_accounts
ORDER BY role;
