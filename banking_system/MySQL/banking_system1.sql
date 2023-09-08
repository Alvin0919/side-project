CREATE DATABASE banking_system;
USE banking_system;
##ALTER TABLE transactions DROP FOREIGN KEY transactions_ibfk_1;
##ALTER TABLE transactions MODIFY recipient_card_id VARCHAR(255) CHARACTER SET utf8mb4;
##ALTER TABLE transactions MODIFY recipient_card_id INT(11) NULL;
##DROP TABLE users;
##DROP TABLE transactions;
CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,            ##id: 自動遞增的用戶 ID，主鍵。
  user_id VARCHAR(255) NOT NULL UNIQUE,         ##user_id: 用戶名，不能為 NULL，且是唯一的。
  card_id VARCHAR(255) NOT NULL UNIQUE,         ##card_id: 用戶的卡號，不能為 NULL，且是唯一的。
  hashed_password VARCHAR(255) NOT NULL,          ##password_hash: 用戶密碼的哈希值，不能為 NULL。
  salt VARCHAR(255) NOT NULL,                   ##salt: 用於加鹽哈希的隨機字串，不能為 NULL。
  balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00  ##balance: 用戶帳戶的餘額，預設為 0。
);

CREATE TABLE transactions (
  id INT AUTO_INCREMENT PRIMARY KEY,                       ##id: 自动递增的交易 ID，主键。
  user_id VARCHAR(255) NOT NULL,                           ##user_id: 执行交易的用户 ID，不能为 NULL，且是 users 表的外键。
  recipient_card_id VARCHAR(255),
  amount DECIMAL(15, 2) NOT NULL,                          ##amount: 交易金额，不能为 NULL。
  transfer_amount DECIMAL(15, 2) NOT NULL,				   ##匯款交易金額
  sender_balance DECIMAL(15, 2),				   ##發送方的餘額
  transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,    ##transaction_time: 交易时间，预设为当前时间。
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  FOREIGN KEY (recipient_card_id) REFERENCES users(card_id)
);

