-- Drop existing database to allow clean re-run
DROP DATABASE IF EXISTS vocab_estimator;
-- =============================================================
-- English Vocabulary Estimation Tool - Database Initialization
-- Database: vocab_estimator
-- MySQL 8.0+
-- =============================================================

CREATE DATABASE IF NOT EXISTS vocab_estimator
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE vocab_estimator;

-- =============================================================
-- Table 1: voc_word - Standard vocabulary library
-- Stores words with difficulty levels and frequency information
-- =============================================================
CREATE TABLE IF NOT EXISTS voc_word (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    word VARCHAR(100) NOT NULL COMMENT 'Word',
    difficulty CHAR(1) NOT NULL COMMENT 'Difficulty: K(Primary), P(Junior), F(Senior), C(College+)',
    frequency DOUBLE DEFAULT 0.5 COMMENT 'Word frequency (0-1), higher = more common',
    definition VARCHAR(500) DEFAULT '' COMMENT 'Chinese definition',
    cet_label VARCHAR(10) DEFAULT 'NONE' COMMENT 'CET label: CET4, CET6, BOTH, NONE',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    UNIQUE KEY uk_word (word),
    INDEX idx_difficulty (difficulty),
    INDEX idx_cet_label (cet_label)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Standard vocabulary library';

-- =============================================================
-- Table 2: user_info - User test records
-- =============================================================
CREATE TABLE IF NOT EXISTS user_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    student_code VARCHAR(50) NOT NULL COMMENT 'Student ID / alias',
    name_alias VARCHAR(50) DEFAULT '' COMMENT 'Name alias',
    cet4_score INT DEFAULT NULL COMMENT 'CET-4 score',
    cet6_score INT DEFAULT NULL COMMENT 'CET-6 score',
    student_type VARCHAR(20) DEFAULT '' COMMENT 'Student type: CORPUS_C/F/P/K',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    UNIQUE KEY uk_student_code (student_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User test records';

-- =============================================================
-- Table 3: test_record - Individual test records
-- =============================================================
CREATE TABLE IF NOT EXISTS test_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    user_id BIGINT NOT NULL COMMENT 'User ID',
    test_words TEXT COMMENT 'Test word list (JSON format)',
    known_count INT DEFAULT 0 COMMENT 'Number of known words',
    unknown_count INT DEFAULT 0 COMMENT 'Number of unknown words',
    estimate_vocab INT DEFAULT 0 COMMENT 'Estimated vocabulary size',
    min_range INT DEFAULT 0 COMMENT 'Lower bound of estimation',
    max_range INT DEFAULT 0 COMMENT 'Upper bound of estimation',
    confidence DOUBLE DEFAULT 0 COMMENT 'Confidence level (0-100)',
    test_type VARCHAR(10) DEFAULT 'GUI' COMMENT 'Test type: GUI, BATCH',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Test time',
    INDEX idx_user_id (user_id),
    INDEX idx_test_type (test_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Individual test records';

-- =============================================================
-- Table 4: batch_task - Batch processing tasks
-- =============================================================
CREATE TABLE IF NOT EXISTS batch_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    word_text TEXT COMMENT 'Uploaded word list text',
    batch_result TEXT COMMENT 'Batch estimation results (JSON)',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'Status: PENDING, PROCESSING, COMPLETED, FAILED',
    remark VARCHAR(500) DEFAULT '' COMMENT 'Remark',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Batch processing tasks';

-- =============================================================
-- Table 5: corpus_data - Learner corpus data (C/F/P/K)
-- =============================================================
CREATE TABLE IF NOT EXISTS corpus_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    corpus_type CHAR(1) NOT NULL COMMENT 'Corpus type: C(College), F(Senior), P(Junior), K(Primary)',
    raw_text LONGTEXT COMMENT 'Raw text content',
    extracted_words TEXT COMMENT 'Extracted word list (JSON array)',
    analysis_result TEXT COMMENT 'Analysis result (JSON)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    INDEX idx_corpus_type (corpus_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Learner corpus data';

-- =============================================================
-- Initial vocabulary data (sample words for all 4 levels)
-- =============================================================

-- K level (Primary school, ~500 basic words)
INSERT INTO voc_word (word, difficulty, frequency, definition, cet_label) VALUES
('apple', 'K', 0.95, 'x', 'NONE'),
('book', 'K', 0.93, 'x', 'NONE'),
('cat', 'K', 0.90, 'x', 'NONE'),
('dog', 'K', 0.92, 'x', 'NONE'),
('egg', 'K', 0.85, 'x', 'NONE'),
('fish', 'K', 0.88, 'x', 'NONE'),
('good', 'K', 0.96, 'x', 'NONE'),
('happy', 'K', 0.87, 'x', 'NONE'),
('ice', 'K', 0.80, 'x', 'NONE'),
('jump', 'K', 0.82, 'x', 'NONE'),
('king', 'K', 0.78, 'x', 'NONE'),
('love', 'K', 0.91, 'x', 'NONE'),
('milk', 'K', 0.86, 'x', 'NONE'),
('name', 'K', 0.94, 'x', 'NONE'),
('old', 'K', 0.90, 'x', 'NONE'),
('pen', 'K', 0.83, 'x', 'NONE'),
('queen', 'K', 0.75, 'x', 'NONE'),
('red', 'K', 0.92, 'x', 'NONE'),
('sun', 'K', 0.88, 'x', 'NONE'),
('tree', 'K', 0.89, 'x', 'NONE'),
('up', 'K', 0.91, 'x', 'NONE'),
('van', 'K', 0.70, 'x', 'NONE'),
('water', 'K', 0.93, 'x', 'NONE'),
('yellow', 'K', 0.84, 'x', 'NONE'),
('zoo', 'K', 0.72, 'x', 'NONE'),
('ball', 'K', 0.85, 'x', 'NONE'),
('cake', 'K', 0.82, 'x', 'NONE'),
('door', 'K', 0.88, 'x', 'NONE'),
('eye', 'K', 0.89, 'x', 'NONE'),
('foot', 'K', 0.86, 'x', 'NONE'),
('girl', 'K', 0.90, 'x', 'NONE'),
('hand', 'K', 0.89, 'x', 'NONE'),
('ink', 'K', 0.65, 'x', 'NONE'),
('jeep', 'K', 0.60, 'x', 'NONE'),
('kite', 'K', 0.70, 'x', 'NONE'),
('lion', 'K', 0.78, 'x', 'NONE'),
('man', 'K', 0.95, 'x', 'NONE'),
('nest', 'K', 0.72, 'x', 'NONE'),
('owl', 'K', 0.68, 'x', 'NONE'),
('pig', 'K', 0.80, 'x', 'NONE'),
('run', 'K', 0.91, 'x', 'NONE'),
('sit', 'K', 0.87, 'x', 'NONE'),
('toy', 'K', 0.76, 'x', 'NONE'),
('use', 'K', 0.90, 'x', 'NONE'),
('vet', 'K', 0.55, 'x', 'NONE'),
('walk', 'K', 0.89, 'x', 'NONE'),
('box', 'K', 0.85, 'x', 'NONE'),
('cry', 'K', 0.78, 'x', 'NONE'),
('day', 'K', 0.94, 'x', 'NONE');

-- P level (Junior high school, ~1500 words)
INSERT INTO voc_word (word, difficulty, frequency, definition, cet_label) VALUES
('abroad', 'P', 0.72, 'x', 'NONE'),
('accept', 'P', 0.85, 'x', 'CET4'),
('accident', 'P', 0.75, 'x', 'CET4'),
('achieve', 'P', 0.80, 'x', 'CET4'),
('address', 'P', 0.88, 'x', 'NONE'),
('advantage', 'P', 0.78, 'x', 'CET4'),
('advise', 'P', 0.76, 'x', 'CET4'),
('afford', 'P', 0.74, 'x', 'CET4'),
('agree', 'P', 0.86, 'x', 'NONE'),
('allow', 'P', 0.85, 'x', 'NONE'),
('although', 'P', 0.82, 'x', 'CET4'),
('amount', 'P', 0.84, 'x', 'CET4'),
('animal', 'P', 0.90, 'x', 'NONE'),
('annual', 'P', 0.72, 'x', 'CET4'),
('anxious', 'P', 0.68, 'x', 'CET4'),
('appear', 'P', 0.83, 'x', 'CET4'),
('arrange', 'P', 0.72, 'x', 'CET4'),
('arrive', 'P', 0.87, 'x', 'NONE'),
('article', 'P', 0.80, 'x', 'CET4'),
('attention', 'P', 0.86, 'x', 'NONE'),
('average', 'P', 0.82, 'x', 'CET4'),
('avoid', 'P', 0.78, 'x', 'CET4'),
('balance', 'P', 0.76, 'x', 'CET4'),
('bargain', 'P', 0.65, 'x', 'CET4'),
('behave', 'P', 0.66, 'x', 'CET4'),
('believe', 'P', 0.88, 'x', 'NONE'),
('belong', 'P', 0.78, 'x', 'CET4'),
('beneath', 'P', 0.64, 'x', 'CET4'),
('besides', 'P', 0.76, 'x', 'CET4'),
('billion', 'P', 0.70, 'x', 'CET4'),
('borrow', 'P', 0.78, 'x', 'NONE'),
('bottom', 'P', 0.80, 'x', 'CET4'),
('branch', 'P', 0.74, 'x', 'CET4'),
('brave', 'P', 0.72, 'x', 'NONE'),
('breath', 'P', 0.76, 'x', 'CET4');

-- F level (Senior high school, ~2500 words)
INSERT INTO voc_word (word, difficulty, frequency, definition, cet_label) VALUES
('abandon', 'F', 0.65, 'x', 'CET4'),
('absorb', 'F', 0.62, 'x', 'CET4'),
('abstract', 'F', 0.55, 'x', 'CET6'),
('abundant', 'F', 0.58, 'x', 'CET6'),
('accelerate', 'F', 0.60, 'x', 'CET6'),
('accompany', 'F', 0.62, 'x', 'CET4'),
('accomplish', 'F', 0.65, 'x', 'CET4'),
('accurate', 'F', 0.64, 'x', 'CET4'),
('accuse', 'F', 0.58, 'x', 'CET4'),
('acknowledge', 'F', 0.60, 'x', 'CET6'),
('acquire', 'F', 0.66, 'x', 'CET4'),
('adapt', 'F', 0.64, 'x', 'CET4'),
('adequate', 'F', 0.62, 'x', 'CET4'),
('adjust', 'F', 0.64, 'x', 'CET4'),
('administration', 'F', 0.60, 'x', 'CET6'),
('adopt', 'F', 0.66, 'x', 'CET4'),
('advance', 'F', 0.70, 'x', 'CET4'),
('advertise', 'F', 0.68, 'x', 'CET4'),
('affair', 'F', 0.72, 'x', 'CET4'),
('affect', 'F', 0.76, 'x', 'CET4'),
('agenda', 'F', 0.58, 'x', 'CET4'),
('aggressive', 'F', 0.60, 'x', 'CET4'),
('allocate', 'F', 0.52, 'x', 'CET6'),
('alternative', 'F', 0.68, 'x', 'CET4'),
('ambition', 'F', 0.60, 'x', 'CET4'),
('analyze', 'F', 0.70, 'x', 'CET4'),
('ancestor', 'F', 0.56, 'x', 'CET4'),
('announce', 'F', 0.72, 'x', 'CET4'),
('anxiety', 'F', 0.56, 'x', 'CET4'),
('apparent', 'F', 0.64, 'x', 'CET4');

-- C level (College and above, ~3500 words)
INSERT INTO voc_word (word, difficulty, frequency, definition, cet_label) VALUES
('abolish', 'C', 0.40, 'x', 'CET6'),
('abortion', 'C', 0.45, 'x', 'CET6'),
('absurd', 'C', 0.38, 'x', 'CET6'),
('abundance', 'C', 0.40, 'x', 'CET6'),
('academy', 'C', 0.52, 'x', 'CET6'),
('accessory', 'C', 0.35, 'x', 'CET6'),
('accommodate', 'C', 0.48, 'x', 'CET6'),
('accountability', 'C', 0.35, 'x', 'CET6'),
('accumulate', 'C', 0.50, 'x', 'CET6'),
('acquaintance', 'C', 0.45, 'x', 'CET6'),
('activate', 'C', 0.42, 'x', 'CET6'),
('acute', 'C', 0.40, 'x', 'CET6'),
('adamant', 'C', 0.30, 'x', 'CET6'),
('adaptation', 'C', 0.45, 'x', 'CET6'),
('adhere', 'C', 0.38, 'x', 'CET6'),
('adjacent', 'C', 0.36, 'x', 'CET6'),
('administer', 'C', 0.42, 'x', 'CET6'),
('adolescent', 'C', 0.44, 'x', 'CET6'),
('adverse', 'C', 0.38, 'x', 'CET6'),
('advocate', 'C', 0.50, 'x', 'CET6'),
('aesthetic', 'C', 0.32, 'x', 'CET6'),
('affiliate', 'C', 0.36, 'x', 'CET6'),
('aggregate', 'C', 0.34, 'x', 'CET6'),
('aggression', 'C', 0.38, 'x', 'CET6'),
('allegation', 'C', 0.35, 'x', 'CET6'),
('allegedly', 'C', 0.35, 'x', 'CET6'),
('ambiguous', 'C', 0.36, 'x', 'CET6'),
('amend', 'C', 0.38, 'x', 'CET6'),
('amplify', 'C', 0.32, 'x', 'CET6'),
('analogy', 'C', 0.34, 'x', 'CET6');

-- Sample test user
INSERT INTO user_info (student_code, name_alias, cet4_score, cet6_score, student_type) VALUES
('TEST_USER_001', 'TStudent', 480, 425, 'CORPUS_C');

-- Sample test record
INSERT INTO test_record (user_id, test_words, known_count, unknown_count, estimate_vocab, min_range, max_range, confidence, test_type) VALUES
(1, '[{"word":"apple","known":true},{"word":"abandon","known":true},{"word":"abolish","known":false}]', 2, 1, 8500, 5000, 12000, 65.0, 'GUI');

