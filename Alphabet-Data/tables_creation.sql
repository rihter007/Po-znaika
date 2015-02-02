CREATE TABLE image (
    _id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
    file_name TEXT NOT NULL,
	
    UNIQUE (file_name) ON CONFLICT FAIL
);

CREATE TABLE sound (
    _id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
    file_name TEXT NOT NULL,
	
    UNIQUE (file_name) ON CONFLICT FAIL
);

/*
Represents special sound selection by their type.
For example: 'Good', 'Well done'
Following categories:
78467623 - сrc32 of 'Correct'
-1022835248 - crc32 of 'Praise'
2010528955 - crc32 of 'TryAgain'
*/
CREATE TABLE special_sound (
    _id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
	type INTEGER NOT NULL,
	sound_id INTEGER NOT NULL,
	
	FOREIGN KEY (sound_id) REFERENCES sound(id),
	
	UNIQUE (sound_id) ON CONFLICT FAIL
);

CREATE TABLE exercise (
    _id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
    type INTEGER,
    name TEXT NOT NULL,
    display_name TEXT NOT NULL,
    image_id INTEGER,
	
    FOREIGN KEY(image_id) REFERENCES image(id),
	
    UNIQUE (name) ON CONFLICT FAIL
);

CREATE TABLE word (
    _id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
    alphabet_id INTEGER NOT NULL,
    word TEXT NOT NULL,
    complexity INTEGER NOT NULL,
	
    UNIQUE (alphabet_id, word) ON CONFLICT FAIL
);

CREATE TABLE word_literal_description (
    _id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
	word_id INTEGER NOT NULL,
	description TEXT NOT NULL,
	
	FOREIGN KEY(word_id) REFERENCES word(_id),
	
	UNIQUE (word_id, description) ON CONFLICT FAIL
);

CREATE TABLE word_image_description (
    _id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
	word_id INTEGER NOT NULL,
	image_id INTEGER NOT NULL,
	
	FOREIGN KEY(word_id) REFERENCES word(_id),
    FOREIGN KEY(image_id) REFERENCES image(_id),
	
    UNIQUE (image_id, word_id) ON CONFLICT FAIL
);

CREATE TABLE word_sound_description (
	_id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
	word_id INTEGER NOT NULL,
	sound_id INTEGER NOT NULL,
	
	FOREIGN KEY(word_id) REFERENCES word(_id),
    FOREIGN KEY(sound_id) REFERENCES sound(_id),
	
    UNIQUE (sound_id, word_id) ON CONFLICT FAIL
);

CREATE TABLE character_exercise (
    _id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
    exercise_id INTEGER NOT NULL,
    character TEXT NOT NULL,
    alphabet_id INTEGER NOT NULL,
    FOREIGN KEY (exercise_id) REFERENCES exercise(id),
    UNIQUE (exercise_id) ON CONFLICT FAIL
);

CREATE TABLE character_exercise_item (
	_id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
	character_exercise_id INTEGER NOT NULL,
	type INTEGER NOT NULL,
	menu_position INTEGER NOT NULL,
	name TEXT NOT NULL,
	display_name TEXT NOT NULL,
	
	FOREIGN KEY(character_exercise_id) REFERENCES character_exercise(_id),
	
	UNIQUE (character_exercise_id, name) ON CONFLICT FAIL,
	UNIQUE (character_exercise_id, menu_position) ON CONFLICT FAIL
);

CREATE TABLE character_exercise_item_step (
	_id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
	character_exercise_item_id INTEGER NOT NULL,
	step_number INTEGER NOT NULL,
	action INTEGER NOT NULL,
	value INTEGER NOT NULL,
	
	FOREIGN KEY(character_exercise_item_id) REFERENCES character_exercise_item(_id)
	
	UNIQUE (character_exercise_item_id, step_number) ON CONFLICT FAIL
);

CREATE TABLE theory_page (
	_id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
	image_id INTEGER,
	sound_id INTEGER,
	message TEXT,
	
	FOREIGN KEY (image_id) REFERENCES image(_id),
	FOREIGN KEY (sound_id) REFERENCES sound(_id),
	UNIQUE(image_id, sound_id, message) ON CONFLICT FAIL
);

-- Represents special table that contain words with certain sound
CREATE TABLE sound_words (
	_id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
	character_exercise_id INTEGER NOT NULL,
	word_id INTEGER NOT NULL,
	sound_flag INTEGER NOT NULL,
	
	FOREIGN KEY (character_exercise_id) REFERENCES character_exercise(_id),
	FOREIGN KEY (word_id) REFERENCES word(_id),
	
	UNIQUE(character_exercise_id, word_id)
);

-- Contains pointers to words which may be used in Game of Word creation from specified.
-- Attention!!! This optimizes the way not to keep ALL russian words!!
CREATE TABLE word_creation_exercise (
    _id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
    word_id INTEGER NOT NULL,
	UNIQUE (word_id) ON CONFLICT FAIL
);

/*
Fill database structure
*/

/*
***************************
Image table
***************************
*/
INSERT INTO image(_id, file_name) VALUES(1, 'database_ch1_print');
INSERT INTO image(_id, file_name) VALUES(2, 'database_ch2_print');
INSERT INTO image(_id, file_name) VALUES(3, 'database_ch3_print');
INSERT INTO image(_id, file_name) VALUES(4, 'database_ch4_print');

INSERT INTO image(_id, file_name) VALUES(5, 'database_ch1_handwrite');
INSERT INTO image(_id, file_name) VALUES(6, 'database_ch2_handwrite');
INSERT INTO image(_id, file_name) VALUES(7, 'database_ch3_handwrite');
INSERT INTO image(_id, file_name) VALUES(8, 'database_ch4_handwrite');

INSERT INTO image(_id, file_name) VALUES(9, 'database_ch1_verse1');
INSERT INTO image(_id, file_name) VALUES(10, 'database_ch1_verse2');
INSERT INTO image(_id, file_name) VALUES(11, 'database_ch2_verse1');
INSERT INTO image(_id, file_name) VALUES(12, 'database_ch4_verse1');

/*Place objects*/
INSERT INTO image(_id, file_name) VALUES(13, 'database_antelope');
INSERT INTO image(_id, file_name) VALUES(14, 'database_asters');
INSERT INTO image(_id, file_name) VALUES(15, 'database_beads');
INSERT INTO image(_id, file_name) VALUES(16, 'database_butterfly');
INSERT INTO image(_id, file_name) VALUES(17, 'database_cloud');
INSERT INTO image(_id, file_name) VALUES(18, 'database_dove');
INSERT INTO image(_id, file_name) VALUES(19, 'database_gnome');
INSERT INTO image(_id, file_name) VALUES(20, 'database_mushroom');
INSERT INTO image(_id, file_name) VALUES(21, 'database_pancake');
INSERT INTO image(_id, file_name) VALUES(22, 'database_raven');
INSERT INTO image(_id, file_name) VALUES(23, 'database_sun');
INSERT INTO image(_id, file_name) VALUES(24, 'database_watermelon');
INSERT INTO image(_id, file_name) VALUES(25, 'database_wolf');

/*
***************************
Sound table
***************************
*/
INSERT INTO sound(_id, file_name) VALUES(1, 'database_sound_test');

/* 
***************************
exercise table
Here:
type column represents hardcoded identifiers of exercise type:
294335127 - crc32 of 'Character'
402850721 - crc32 of 'WordGather'
-858355490 - crc32 of 'CreateWordsFromSpecified' 
***************************
*/
INSERT INTO exercise(_id, type, name, display_name, image_id) VALUES(699588759,   294335127, 'Russian.Alphabet.Ch1', 'Буква А', 1);
INSERT INTO exercise(_id, type, name, display_name, image_id) VALUES(-1329876691, 294335127, 'Russian.Alphabet.Ch2', 'Буква Б', 2);
INSERT INTO exercise(_id, type, name, display_name, image_id) VALUES(-943947333,  294335127, 'Russian.Alphabet.Ch3', 'Буква В', 3);
INSERT INTO exercise(_id, type, name, display_name, image_id) VALUES(1507333144,  294335127, 'Russian.Alphabet.Ch4', 'Буква Г', 4);

-- Games
INSERT INTO exercise(_id, type, name, display_name, image_id) VALUES(916623525,   402850721, 'Russian.Alphabet.WordGather', 'Слово рассыпалось', 5);
INSERT INTO exercise(_id, type, name, display_name, image_id) VALUES(1264890976,   -858355490, 'Russian.Alphabet.CreateWordsFromSpecified', 'Составь слова', 6);

/* 
***************************
character_exercise table
Here:
alphabet_id is column of hardocded alphabet identifier
92928056 - crc32 of 'ru'
***************************
*/
INSERT INTO character_exercise(_id, exercise_id, character, alphabet_id) VALUES(1, 699588759,   'а', 2092928056); 
INSERT INTO character_exercise(_id, exercise_id, character, alphabet_id) VALUES(2, -1329876691, 'б', 2092928056); 
INSERT INTO character_exercise(_id, exercise_id, character, alphabet_id) VALUES(3, -943947333,  'в', 2092928056); 
INSERT INTO character_exercise(_id, exercise_id, character, alphabet_id) VALUES(4, 1507333144,  'г', 2092928056);

/* 
***************************
character_exercise_item table
Here:
type column is hardcoded character_exercise_item type
26480598 a crc32 of 'General'
961539200 a crc32 of 'Sound'
-1985025220 a ccr32 of 'Letter'
***************************
*/
INSERT INTO character_exercise_item(_id, character_exercise_id, type, menu_position, name, display_name)
    VALUES(-614874078, 1, 26480598, 0, 'Alphabet.Russian.Character1.General', 'Буква А');
INSERT INTO character_exercise_item(_id, character_exercise_id, type, menu_position, name, display_name)
    VALUES(-614874078, 2, 961539200, 1, 'Alphabet.Russian.Character1.Sound', 'Звук буквы А');
INSERT INTO character_exercise_item(_id, character_exercise_id, type, menu_position, name, display_name)
    VALUES(-614874078, 2, -1985025220, 1, 'Alphabet.Russian.Character1.Letter', 'Написание буквы А');
	
/* 
***************************
character_exercise_item_step table
Here:
action is a column which specifies what to do
1986991965 - a crc32 of 'TheoryPage'. Shows a single page of theory. value column specifies an identifier in theory_page table
291784361 - a crc32 of 'CustomAction'. Depends on character_exercise_item_id.
***************************
*/
INSERT INTO character_exercise_item_step(_id, character_exercise_item_id, step_number, action, value)
    VALUES(1, -614874078, 0, 1986991965, 1);
INSERT INTO character_exercise_item_step(_id, character_exercise_item_id, step_number, action, value)
    VALUES(2, -614874078, 1, 291784361, 0);
INSERT INTO character_exercise_item_step(_id, character_exercise_item_id, step_number, action, value)
    VALUES(3, -614874078, 2, 291784361, 1);
	
/* 
***************************
theory_page table
***************************
*/
INSERT INTO theory_page(_id, image_id, sound_id, message) VALUES(1, 4, 1, 'Арбуз');


/* 
***************************
word table
***************************
*/
-- For create words exercise
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(1,    2092928056, 'консервы', 3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(2,    2092928056, 'кон', 1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(3,    2092928056, 'нерв', 1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(4,    2092928056, 'сок', 1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(5,    2092928056, 'ров', 1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(6,    2092928056, 'вор', 1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(7,    2092928056, 'сор', 1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(8,    2092928056, 'вес', 1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(9,    2092928056, 'весы', 2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(10,   2092928056, 'сыр', 1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(11,   2092928056, 'нос', 1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(12,   2092928056, 'сон', 1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(13,   2092928056, 'рык', 1);

-- Different objects
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(14,    2092928056, 'антилопа', 4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(15,    2092928056, 'арбуз', 2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(16,    2092928056, 'астры', 2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(17,    2092928056, 'бабочка', 3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(18,    2092928056, 'блин', 1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(19,    2092928056, 'бусы', 1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(20,    2092928056, 'облако', 3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(21,    2092928056, 'голубь', 2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(22,    2092928056, 'баба', 2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(23,    2092928056, 'гном', 1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(24,    2092928056, 'гриб', 1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(25,    2092928056, 'ворона', 3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES(26,    2092928056, 'солнце', 2);

/* 
***************************
word_image_description table
***************************
*/
INSERT INTO word_image_description(word_id, image_id) VALUES(14, 13);
INSERT INTO word_image_description(word_id, image_id) VALUES(15, 24);
INSERT INTO word_image_description(word_id, image_id) VALUES(16, 14);
INSERT INTO word_image_description(word_id, image_id) VALUES(17, 16);
INSERT INTO word_image_description(word_id, image_id) VALUES(18, 21);
INSERT INTO word_image_description(word_id, image_id) VALUES(19, 15);
INSERT INTO word_image_description(word_id, image_id) VALUES(20, 17);
INSERT INTO word_image_description(word_id, image_id) VALUES(21, 18);
INSERT INTO word_image_description(word_id, image_id) VALUES(23, 19);
INSERT INTO word_image_description(word_id, image_id) VALUES(24, 20);
INSERT INTO word_image_description(word_id, image_id) VALUES(25, 22);
INSERT INTO word_image_description(word_id, image_id) VALUES(26, 23);

/* 
***************************
word_sound_description table
***************************
*/
INSERT INTO word_sound_description(word_id, sound_id) VALUES(14, 1);
INSERT INTO word_sound_description(word_id, sound_id) VALUES(15, 1);
INSERT INTO word_sound_description(word_id, sound_id) VALUES(16, 1);
INSERT INTO word_sound_description(word_id, sound_id) VALUES(17, 1);
INSERT INTO word_sound_description(word_id, sound_id) VALUES(18, 1);
INSERT INTO word_sound_description(word_id, sound_id) VALUES(19, 1);
INSERT INTO word_sound_description(word_id, sound_id) VALUES(20, 1);
INSERT INTO word_sound_description(word_id, sound_id) VALUES(21, 1);
INSERT INTO word_sound_description(word_id, sound_id) VALUES(23, 1);
INSERT INTO word_sound_description(word_id, sound_id) VALUES(24, 1);
INSERT INTO word_sound_description(word_id, sound_id) VALUES(25, 1);
INSERT INTO word_sound_description(word_id, sound_id) VALUES(26, 1);

/* 
***************************
word_literal_description table
***************************
*/
INSERT INTO word_literal_description(word_id, description) VALUES(14, 'Антилопа - от греч. рогатое животное, название некоторых подсемейств и родов полорогих');

/* 
***************************
sound_words table
***************************
*/
INSERT INTO sound_words(character_exercise_id, word_id, sound_flag) VALUES(1, 14, 7);
INSERT INTO sound_words(character_exercise_id, word_id, sound_flag) VALUES(1, 15, 3);
INSERT INTO sound_words(character_exercise_id, word_id, sound_flag) VALUES(1, 16, 3);
INSERT INTO sound_words(character_exercise_id, word_id, sound_flag) VALUES(1, 17, 1);
INSERT INTO sound_words(character_exercise_id, word_id, sound_flag) VALUES(1, 22, 5);

INSERT INTO sound_words(character_exercise_id, word_id, sound_flag) VALUES(2, 17, 3);
INSERT INTO sound_words(character_exercise_id, word_id, sound_flag) VALUES(2, 18, 3);
INSERT INTO sound_words(character_exercise_id, word_id, sound_flag) VALUES(2, 19, 3);
INSERT INTO sound_words(character_exercise_id, word_id, sound_flag) VALUES(2, 22, 3);
INSERT INTO sound_words(character_exercise_id, word_id, sound_flag) VALUES(2, 24, 5);

/* 
***************************
word_creation_exercise table
***************************
*/
INSERT INTO word_creation_exercise(word_id) VALUES(1);