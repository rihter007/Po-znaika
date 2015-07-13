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

CREATE TABLE verse (
	_id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
	alphabet_id INTEGER NOT NULL,
	verse_text TEXT NOT NULL,
	
	UNIQUE(alphabet_id, verse_text) ON CONFLICT FAIL
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

CREATE TABLE exercise (
    _id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
    type INTEGER,
    name TEXT NOT NULL,
	max_score INTEGER,
		
    UNIQUE (name) ON CONFLICT FAIL
);

CREATE TABLE exercise_display_name (
	_id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
	exercise_id INTEGER NOT NULL,
	alphabet_id INTEGER NOT NULL,
	display_name TEXT NOT NULL,

	FOREIGN KEY (exercise_id) REFERENCES exercise(_id),
	
	UNIQUE (exercise_id, alphabet_id) ON CONFLICT FAIL
);
	
CREATE TABLE character_exercise (
    _id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
	alphabet_type INTEGER NOT NULL,
    character TEXT NOT NULL,    
	not_passed_image_id INTEGER NOT NULL,
	passed_image_id INTEGER NOT NULL,
		
    FOREIGN KEY (not_passed_image_id) REFERENCES image(_id),
	FOREIGN KEY (passed_image_id) REFERENCES image(_id),
	
	UNIQUE (alphabet_type, character) ON CONFLICT FAIL
);

CREATE TABLE character_exercise_item (
	_id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
	exercise_id INTEGER NOT NULL,
	character_exercise_id INTEGER NOT NULL,
	icon_id INTEGER,
	menu_element_type INTEGER NOT NULL,
	
	FOREIGN KEY(exercise_id) REFERENCES exercise(_id),
	FOREIGN KEY(character_exercise_id) REFERENCES character_exercise(_id),
	FOREIGN KEY(icon_id) REFERENCES image(_id),
	
	UNIQUE (character_exercise_id, exercise_id) ON CONFLICT FAIL,
	UNIQUE (character_exercise_id, menu_element_type) ON CONFLICT FAIL
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
	image_redirect_url TEXT,
	sound_id INTEGER,
	message TEXT,
	
	FOREIGN KEY (image_id) REFERENCES image(_id),
	FOREIGN KEY (sound_id) REFERENCES sound(_id),
	UNIQUE(image_id, image_redirect_url, sound_id, message) ON CONFLICT FAIL
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
INSERT INTO image(_id, file_name) VALUES (1,'database_russian_ch1_print');
INSERT INTO image(_id, file_name) VALUES (2,'database_russian_ch1_handwrite');
INSERT INTO image(_id, file_name) VALUES (3,'database_horse');
INSERT INTO image(_id, file_name) VALUES (4,'database_owl');
INSERT INTO image(_id, file_name) VALUES (5,'database_stork');
INSERT INTO image(_id, file_name) VALUES (6,'database_russian_ch2_print');
INSERT INTO image(_id, file_name) VALUES (7,'database_russian_ch2_handwrite');
INSERT INTO image(_id, file_name) VALUES (8,'database_butterfly');
INSERT INTO image(_id, file_name) VALUES (9,'database_mushroom');
INSERT INTO image(_id, file_name) VALUES (10,'database_squirrel');
INSERT INTO image(_id, file_name) VALUES (11,'database_russian_ch3_print');
INSERT INTO image(_id, file_name) VALUES (12,'database_russian_ch3_handwrite');
INSERT INTO image(_id, file_name) VALUES (13,'database_cow');
INSERT INTO image(_id, file_name) VALUES (14,'database_island');
INSERT INTO image(_id, file_name) VALUES (15,'database_wolf');
INSERT INTO image(_id, file_name) VALUES (16,'database_russian_ch4_print');
INSERT INTO image(_id, file_name) VALUES (17,'database_russian_ch4_handwrite');
INSERT INTO image(_id, file_name) VALUES (18,'database_gnome');
INSERT INTO image(_id, file_name) VALUES (19,'database_hippopotamus');
INSERT INTO image(_id, file_name) VALUES (20,'database_iron');
INSERT INTO image(_id, file_name) VALUES (21,'database_russian_ch5_print');
INSERT INTO image(_id, file_name) VALUES (22,'database_russian_ch5_handwrite');
INSERT INTO image(_id, file_name) VALUES (23,'database_house');
INSERT INTO image(_id, file_name) VALUES (24,'database_rainbow');
INSERT INTO image(_id, file_name) VALUES (25,'database_trail');
INSERT INTO image(_id, file_name) VALUES (26,'database_russian_ch6_print');
INSERT INTO image(_id, file_name) VALUES (27,'database_russian_ch6_handwrite');
INSERT INTO image(_id, file_name) VALUES (28,'database_cake');
INSERT INTO image(_id, file_name) VALUES (29,'database_raccoon');
INSERT INTO image(_id, file_name) VALUES (30,'database_snake');
INSERT INTO image(_id, file_name) VALUES (31,'database_russian_ch7_print');
INSERT INTO image(_id, file_name) VALUES (32,'database_russian_ch7_handwrite');
INSERT INTO image(_id, file_name) VALUES (33,'database_gun');
INSERT INTO image(_id, file_name) VALUES (34,'database_hedgehog');
INSERT INTO image(_id, file_name) VALUES (35,'database_honey');
INSERT INTO image(_id, file_name) VALUES (36,'database_russian_ch8_print');
INSERT INTO image(_id, file_name) VALUES (37,'database_russian_ch8_handwrite');
INSERT INTO image(_id, file_name) VALUES (38,'database_ice_cream');
INSERT INTO image(_id, file_name) VALUES (39,'database_toad');
INSERT INTO image(_id, file_name) VALUES (40,'database_walrus');
INSERT INTO image(_id, file_name) VALUES (41,'database_russian_ch9_print');
INSERT INTO image(_id, file_name) VALUES (42,'database_russian_ch9_handwrite');
INSERT INTO image(_id, file_name) VALUES (43,'database_nest');
INSERT INTO image(_id, file_name) VALUES (44,'database_watermelon');
INSERT INTO image(_id, file_name) VALUES (45,'database_zebra');
INSERT INTO image(_id, file_name) VALUES (46,'database_russian_ch10_print');
INSERT INTO image(_id, file_name) VALUES (47,'database_russian_ch10_handwrite');
INSERT INTO image(_id, file_name) VALUES (48,'database_car');
INSERT INTO image(_id, file_name) VALUES (49,'database_thread');
INSERT INTO image(_id, file_name) VALUES (50,'database_turkey');
INSERT INTO image(_id, file_name) VALUES (51,'database_russian_ch11_print');
INSERT INTO image(_id, file_name) VALUES (52,'database_russian_ch11_handwrite');
INSERT INTO image(_id, file_name) VALUES (53,'database_balalaika');
INSERT INTO image(_id, file_name) VALUES (54,'database_sparrow');
INSERT INTO image(_id, file_name) VALUES (55,'database_yogurt');
INSERT INTO image(_id, file_name) VALUES (56,'database_russian_ch12_print');
INSERT INTO image(_id, file_name) VALUES (57,'database_russian_ch12_handwrite');
INSERT INTO image(_id, file_name) VALUES (58,'database_carrot');
INSERT INTO image(_id, file_name) VALUES (59,'database_crab');
INSERT INTO image(_id, file_name) VALUES (60,'database_poppy');
INSERT INTO image(_id, file_name) VALUES (61,'database_russian_ch13_print');
INSERT INTO image(_id, file_name) VALUES (62,'database_russian_ch13_handwrite');
INSERT INTO image(_id, file_name) VALUES (63,'database_goat');
INSERT INTO image(_id, file_name) VALUES (64,'database_orange');
INSERT INTO image(_id, file_name) VALUES (65,'database_swallow');
INSERT INTO image(_id, file_name) VALUES (66,'database_russian_ch14_print');
INSERT INTO image(_id, file_name) VALUES (67,'database_russian_ch14_handwrite');
INSERT INTO image(_id, file_name) VALUES (68,'database_bear');
INSERT INTO image(_id, file_name) VALUES (69,'database_catfish');
INSERT INTO image(_id, file_name) VALUES (70,'database_mosquito');
INSERT INTO image(_id, file_name) VALUES (71,'database_russian_ch15_print');
INSERT INTO image(_id, file_name) VALUES (72,'database_russian_ch15_handwrite');
INSERT INTO image(_id, file_name) VALUES (73,'database_rhino');
INSERT INTO image(_id, file_name) VALUES (74,'database_sun');
INSERT INTO image(_id, file_name) VALUES (75,'database_drum');
INSERT INTO image(_id, file_name) VALUES (76,'database_russian_ch16_print');
INSERT INTO image(_id, file_name) VALUES (77,'database_russian_ch16_handwrite');
INSERT INTO image(_id, file_name) VALUES (78,'database_beaver');
INSERT INTO image(_id, file_name) VALUES (79,'database_grain');
INSERT INTO image(_id, file_name) VALUES (80,'database_monkey');
INSERT INTO image(_id, file_name) VALUES (81,'database_russian_ch17_print');
INSERT INTO image(_id, file_name) VALUES (82,'database_russian_ch17_handwrite');
INSERT INTO image(_id, file_name) VALUES (83,'database_hat');
INSERT INTO image(_id, file_name) VALUES (84,'database_syrup');
INSERT INTO image(_id, file_name) VALUES (85,'database_penguin');
INSERT INTO image(_id, file_name) VALUES (86,'database_russian_ch18_print');
INSERT INTO image(_id, file_name) VALUES (87,'database_russian_ch18_handwrite');
INSERT INTO image(_id, file_name) VALUES (88,'database_big_cake');
INSERT INTO image(_id, file_name) VALUES (89,'database_camomile');
INSERT INTO image(_id, file_name) VALUES (90,'database_clover');
INSERT INTO image(_id, file_name) VALUES (91,'database_russian_ch19_print');
INSERT INTO image(_id, file_name) VALUES (92,'database_russian_ch19_handwrite');
INSERT INTO image(_id, file_name) VALUES (93,'database_bonfire');
INSERT INTO image(_id, file_name) VALUES (94,'database_compass');
INSERT INTO image(_id, file_name) VALUES (95,'database_dog');
INSERT INTO image(_id, file_name) VALUES (96,'database_russian_ch20_print');
INSERT INTO image(_id, file_name) VALUES (97,'database_russian_ch20_handwrite');
INSERT INTO image(_id, file_name) VALUES (98,'database_cat');
INSERT INTO image(_id, file_name) VALUES (99,'database_chair');
INSERT INTO image(_id, file_name) VALUES (100,'database_tv_set');
INSERT INTO image(_id, file_name) VALUES (101,'database_russian_ch21_print');
INSERT INTO image(_id, file_name) VALUES (102,'database_russian_ch21_handwrite');
INSERT INTO image(_id, file_name) VALUES (103,'database_hive');
INSERT INTO image(_id, file_name) VALUES (104,'database_corn');
INSERT INTO image(_id, file_name) VALUES (105,'database_kangaroo');
INSERT INTO image(_id, file_name) VALUES (106,'database_russian_ch22_print');
INSERT INTO image(_id, file_name) VALUES (107,'database_russian_ch22_handwrite');
INSERT INTO image(_id, file_name) VALUES (108,'database_candy');
INSERT INTO image(_id, file_name) VALUES (109,'database_fountain');
INSERT INTO image(_id, file_name) VALUES (110,'database_giraffe');
INSERT INTO image(_id, file_name) VALUES (111,'database_russian_ch23_print');
INSERT INTO image(_id, file_name) VALUES (112,'database_russian_ch23_handwrite');
INSERT INTO image(_id, file_name) VALUES (113,'database_bread');
INSERT INTO image(_id, file_name) VALUES (114,'database_streamship');
INSERT INTO image(_id, file_name) VALUES (115,'database_sunflower');
INSERT INTO image(_id, file_name) VALUES (116,'database_russian_ch24_print');
INSERT INTO image(_id, file_name) VALUES (117,'database_russian_ch24_handwrite');
INSERT INTO image(_id, file_name) VALUES (118,'database_chicken');
INSERT INTO image(_id, file_name) VALUES (119,'database_hare');
INSERT INTO image(_id, file_name) VALUES (120,'database_hen');
INSERT INTO image(_id, file_name) VALUES (121,'database_russian_ch25_print');
INSERT INTO image(_id, file_name) VALUES (122,'database_russian_ch25_handwrite');
INSERT INTO image(_id, file_name) VALUES (123,'database_ball');
INSERT INTO image(_id, file_name) VALUES (124,'database_country_house');
INSERT INTO image(_id, file_name) VALUES (125,'database_tortoise');
INSERT INTO image(_id, file_name) VALUES (126,'database_russian_ch26_print');
INSERT INTO image(_id, file_name) VALUES (127,'database_russian_ch26_handwrite');
INSERT INTO image(_id, file_name) VALUES (128,'database_cup');
INSERT INTO image(_id, file_name) VALUES (129,'database_fur_coat');
INSERT INTO image(_id, file_name) VALUES (130,'database_ruff');
INSERT INTO image(_id, file_name) VALUES (131,'database_russian_ch27_print');
INSERT INTO image(_id, file_name) VALUES (132,'database_russian_ch27_handwrite');
INSERT INTO image(_id, file_name) VALUES (133,'database_cloak');
INSERT INTO image(_id, file_name) VALUES (134,'database_condensed_milk');
INSERT INTO image(_id, file_name) VALUES (135,'database_goldfinch');
INSERT INTO image(_id, file_name) VALUES (136,'database_russian_ch28_print');
INSERT INTO image(_id, file_name) VALUES (137,'database_russian_ch28_handwrite');
INSERT INTO image(_id, file_name) VALUES (138,'database_hug');
INSERT INTO image(_id, file_name) VALUES (139,'database_russian_ch29_print');
INSERT INTO image(_id, file_name) VALUES (140,'database_russian_ch29_handwrite');
INSERT INTO image(_id, file_name) VALUES (141,'database_balloons');
INSERT INTO image(_id, file_name) VALUES (142,'database_mouse');
INSERT INTO image(_id, file_name) VALUES (143,'database_russian_ch30_print');
INSERT INTO image(_id, file_name) VALUES (144,'database_russian_ch30_handwrite');
INSERT INTO image(_id, file_name) VALUES (145,'database_balls');
INSERT INTO image(_id, file_name) VALUES (146,'database_bumblebee');
INSERT INTO image(_id, file_name) VALUES (147,'database_russian_ch31_print');
INSERT INTO image(_id, file_name) VALUES (148,'database_russian_ch31_handwrite');
INSERT INTO image(_id, file_name) VALUES (149,'database_airport');
INSERT INTO image(_id, file_name) VALUES (150,'database_aloe');
INSERT INTO image(_id, file_name) VALUES (151,'database_excavator');
INSERT INTO image(_id, file_name) VALUES (152,'database_russian_ch32_print');
INSERT INTO image(_id, file_name) VALUES (153,'database_russian_ch32_handwrite');
INSERT INTO image(_id, file_name) VALUES (154,'database_menu');
INSERT INTO image(_id, file_name) VALUES (155,'database_thumbelina');
INSERT INTO image(_id, file_name) VALUES (156,'database_whirligig');
INSERT INTO image(_id, file_name) VALUES (157,'database_russian_ch33_print');
INSERT INTO image(_id, file_name) VALUES (158,'database_russian_ch33_handwrite');
INSERT INTO image(_id, file_name) VALUES (159,'database_apple');
INSERT INTO image(_id, file_name) VALUES (160,'database_family');
INSERT INTO image(_id, file_name) VALUES (161,'database_moon');

-- Insert character images
INSERT INTO image(_id, file_name) VALUES (300,'database_russian_icon_dark_ch1');
INSERT INTO image(_id, file_name) VALUES (301,'database_russian_icon_light_ch1');
INSERT INTO image(_id, file_name) VALUES (302,'database_russian_icon_dark_ch2');
INSERT INTO image(_id, file_name) VALUES (303,'database_russian_icon_light_ch2');

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

CREATE TABLE exercise (
    _id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
    type INTEGER,
    name TEXT NOT NULL,
	max_score INTEGER,
		
    UNIQUE (name) ON CONFLICT FAIL
);

***************************
*/

-- Character exercises

INSERT INTO exercise(_id, type, name, max_score) VALUES(1195583655, 294335127, 'Alphabet.Russian.Character1.Sound', 10);
INSERT INTO exercise(_id, type, name, max_score) VALUES(-1441032379, 294335127, 'Alphabet.Russian.Character1.Print', 10);
INSERT INTO exercise(_id, type, name, max_score) VALUES(-588490738, 294335127, 'Alphabet.Russian.Character1.Handwrite', 10);
INSERT INTO exercise(_id, type, name, max_score) VALUES(-1101215443, 294335127, 'Alphabet.Russian.Character1.FindImage', 50);
INSERT INTO exercise(_id, type, name, max_score) VALUES(1879715682, 294335127, 'Alphabet.Russian.Character1.FindCharacter', 50);

-- Games
INSERT INTO exercise(_id, type, name, max_score) VALUES(916623525,   402850721, 'Alphabet.Russian.WordGather', 100);
INSERT INTO exercise(_id, type, name, max_score) VALUES(1264890976,   -858355490, 'Alphabet.Russian.CreateWordsFromSpecified', 100);

/* 
***************************
character_exercise table
Here:
alphabet_id is column of hardocded alphabet identifier
-345575051- crc32 of 'russian'

CREATE TABLE character_exercise (
    _id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
    character TEXT NOT NULL,
    alphabet_type INTEGER NOT NULL,
	not_passed_image_id INTEGER NOT NULL,
	passed_image_id INTEGER NOT NULL,
		
    FOREIGN KEY (not_passed_image_id) REFERENCES image(_id),
	FOREIGN KEY (passed_image_id) REFERENCES image(_id),
	
	UNIQUE (alphabet_id, character) ON CONFLICT FAIL
);
***************************
*/
INSERT INTO character_exercise(_id, character, alphabet_type, not_passed_image_id, passed_image_id) VALUES(1, 'а', -345575051, 300, 301); 
--INSERT INTO character_exercise(_id, character, alphabet_type, not_passed_image_id, passed_image_id) VALUES(2, 'б', -345575051, 302, 303);

/* 
***************************
character_exercise_item table

CREATE TABLE character_exercise_item (
	_id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
	exercise_id INTEGER NOT NULL,
	character_exercise_id INTEGER NOT NULL,
	menu_element_type INTEGER NOT NULL,
	not_passed_image_id INTEGER NOT NULL,
	started_image_id INTEGER NOT NULL,
	passed_image_id INTEGER NOT NULL,
	
	FOREIGN KEY(exercise_id) REFERENCES exercise(_id),
	FOREIGN KEY(character_exercise_id) REFERENCES character_exercise(_id),
	
	UNIQUE (character_exercise_id, exercise_id) ON CONFLICT FAIL,
	UNIQUE (character_exercise_id, menu_element_type) ON CONFLICT FAIL
);

CharacterSound(260157427),            // a crc32 of 'CharacterExerciseItemType.CharacterSound'
CharacterPrint(-489091055),           // a crc32 of 'CharacterExerciseItemType.CharacterPrint'
CharacterHandWrite(1022598804),       // a crc32 of 'CharacterExerciseItemType.CharacterHandWrite'
FindPictureWithCharacter(-954576837), // a crc32 of 'CharacterExerciseItemType.FindPictureWithCharacter'
FindCharacter(-226661029);            // a crc32 of 'CharacterExerciseItemType.FindCharacter'

***************************
*/
/* Character 1 */
INSERT INTO character_exercise_item(_id, exercise_id, character_exercise_id, menu_element_type) VALUES (1, 1195583655, 1, 260157427);
INSERT INTO character_exercise_item(_id, exercise_id, character_exercise_id, menu_element_type) VALUES (2, -1441032379, 1, -489091055);
INSERT INTO character_exercise_item(_id, exercise_id, character_exercise_id, menu_element_type) VALUES (3, -588490738, 1, 1022598804);
INSERT INTO character_exercise_item(_id, exercise_id, character_exercise_id, menu_element_type) VALUES (4, -1101215443, 1, -954576837);
INSERT INTO character_exercise_item(_id, exercise_id, character_exercise_id, menu_element_type) VALUES (5, 1879715682, 1, -226661029);
	
/* 
***************************
character_exercise_item_step table
Here:
action is a column which specifies what to do
1986991965 - a crc32 of 'TheoryPage'. Shows a single page of theory. value column specifies an identifier in theory_page table
291784361 - a crc32 of 'CustomAction'. Depends on character_exercise_item_id: 
	0 - fragment with multiple words/sound/images for that SOUND (just for information: user clicks on image, hears the sound, views the object)
	1 - fragment with selection of words for that CHARACTER
	2 - fragment with selection of specified CHARACTER in verse
***************************
*/

/* Sound ch1 */
INSERT INTO character_exercise_item_step(_id, character_exercise_item_id, step_number, action, value)
    VALUES(3, 1195583655, 2, 1986991965, 1);
INSERT INTO character_exercise_item_step(_id, character_exercise_item_id, step_number, action, value)
    VALUES(4, 1195583655, 3, 1986991965, 2);
INSERT INTO character_exercise_item_step(_id, character_exercise_item_id, step_number, action, value)
    VALUES(5, 1195583655, 4, 1986991965, 3);
INSERT INTO character_exercise_item_step(_id, character_exercise_item_id, step_number, action, value)
    VALUES(6, 1195583655, 5, 1986991965, 4);
	
/* Print ch1 */
INSERT INTO character_exercise_item_step(_id, character_exercise_item_id, step_number, action, value)
    VALUES(11, 1196999958, 0, 1986991965, 11);
--INSERT INTO character_exercise_item_step(_id, character_exercise_item_id, step_number, action, value)
--    VALUES(12, 1196999958, 1, 1986991965, 12);
INSERT INTO character_exercise_item_step(_id, character_exercise_item_id, step_number, action, value)
    VALUES(13, 1196999958, 2, 1986991965, 2);
INSERT INTO character_exercise_item_step(_id, character_exercise_item_id, step_number, action, value)
    VALUES(14, 1196999958, 3, 1986991965, 14);
INSERT INTO character_exercise_item_step(_id, character_exercise_item_id, step_number, action, value)
    VALUES(15, 1196999958, 4, 1986991965, 15);

/* Handwrite ch1 */
--INSERT INTO character_exercise_item_step(_id, character_exercise_item_id, step_number, action, value)
--	VALUES(21, -588490738, 0, 1986991965, 21)
--INSERT INTO character_exercise_item_step(_id, character_exercise_item_id, step_number, action, value)
--	VALUES(21, -588490738, 1, 1986991965, 23)

/* FindImage ch1 */
INSERT INTO character_exercise_item_step(_id, character_exercise_item_id, step_number, action, value)
	VALUES(31, -1101215443, 0, 291784361, 2);
	
/* Find character ch1 */
INSERT INTO character_exercise_item_step(_id, character_exercise_item_id, step_number, action, value)
	VALUES(41, 1879715682, 0, 291784361, 3);
	
/*
INSERT INTO character_exercise_item_step(_id, character_exercise_item_id, step_number, action, value)
    VALUES(2, 1195583655, 1, 291784361, 0);
INSERT INTO character_exercise_item_step(_id, character_exercise_item_id, step_number, action, value)
    VALUES(3, 1195583655, 2, 291784361, 1);
*/
/* 
***************************
theory_page table
***************************
*/
/*
Availible formatters:
- <format color='RGB code', size=''></format>
*/

-- Character 1

-- Sound of character 1
INSERT INTO theory_page(_id, image_id, sound_id, message) VALUES(1, 200, 0, 'Бывает ударный и безударный');
INSERT INTO theory_page(_id, image_id, sound_id, message) VALUES(2, 201, 0, 'а');
INSERT INTO theory_page(_id, image_id, sound_id, message) VALUES(3, 400, 0, '<format color=''ffff0000''>А</format>ист');
INSERT INTO theory_page(_id, image_id, sound_id, message) VALUES(4, 404, 0, 'Белк<format color=''ffff0000''>а</format>');

-- Print character 1
INSERT INTO theory_page(_id, image_id, sound_id, message) VALUES(11, 1, 0, 'Печатный вариант буквы ''А''');
--INSERT INTO theory_page(_id, image_id, sound_id, message) VALUES(12, 5, 0, 'Прописной вариант буквы ''а''');
--INSERT INTO theory_page(_id, image_id, sound_id, message) VALUES(13, 400, 0, '<format color=''ffff0000''>А</format>ист');
INSERT INTO theory_page(_id, image_id, sound_id, message) VALUES(14, 401, 0, 'лош<format color=''ffff0000''>а</format>дь');
INSERT INTO theory_page(_id, image_id, sound_id, message) VALUES(15, 402, 0, 'сов<format color=''ffff0000''>а</format>');

-- Handwrite character 1
--INSERT INTO theory_page(_id, image_id, sound_id, message) VALUES(21, 5, 0, 'Прописаной вариант буквы ''а''');
--INSERT INTO theory_page(_id, image_id, image_redirect_url, sound_id, message) VALUES (23, 5, 'http://m.youtube.com/watch?v=_ACJM-ratEg', 0, NULL);



/* 
***************************
verse table
***************************
*/
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (1,-345575051,'Аист нам доставил груз:</br>Аппетитнейший арбуз,</br>Ананасы, мандарины,</br>Абрикосы, апельсины!');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (2,-345575051,'Тары-бары, тары-бары,</br>Зайцы ехали с базара,</br>Прикупили для зимовки </br>Тридцать пять мешков морковки.');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (3,-345575051,'Тихо, словно бы во сне, </br>Закружился в вышине </br>Первый снег </br>И зашептал: </br>- Как давно я не летал!');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (4,-345575051,'Снег на улице и стужа. </br>Крепким льдом забита лужа. </br>От дороги робко </br>Веточками тропки </br>Тянутся к домам: </br>— Погреться можно к вам?');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (5,-345575051,'Золотила осень лес - </br>Ювелирная работа. </br>А он взял и вдруг облез - </br>Вся исчезла позолота. ');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (6,-345575051,'В зимний день я не скучаю:</br>Быстро лыжи надеваю,</br>В руки я беру две палки,</br>С ветерком играю в салки! ');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (7,-345575051,'Ущипнул меня за нос</br>Разыгравшийся мороз!</br>Носик я потёр ладошкой,</br>По земле потопал ножкой.</br>- Не щипай меня, мороз,</br>Я и так уже замерз!');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (8,-345575051,'Ах, капель, капель, капель, </br>Золотая карусель! </br>К нам в кораблике бумажном </br>По ручью приплыл Апрель!');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (9,-345575051,'Как это приятно —</br>Проснуться</br>И встать,</br>И синее небо</br>В окне увидать,</br>И снова узнать,</br>Что повсюду — весна,</br>Что утро и солнце</br>Прекраснее сна!');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (10,-345575051,'Солнце в небе синем</br>Пахнет апельсином,</br>Яблоком румяным,</br>Ягодой с поляны.</br>А еще подсолнушком</br>Пахнет наше солнышко!');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (11,-345575051,'Сверху небо – высоко, </br>В луже небо – глубоко.</br>Если в лужу оступиться, </br>Можно в небо провалиться!');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (12,-345575051,'Божья коровка – </br>Маленький жучок. </br>Чёрная головка </br>В пятнышках бочок. </br>На ладошке ножками </br>Не ползи на край! </br>Будь моею брошкою </br>И не улетай!');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (13,-345575051,'Теплый дождик лил да лил,</br>Дело делал, не шалил:</br>Вымыл крышу у скворешни,</br>Вымыл ягоды – черешни…</br>Протянули мы ладошки,</br>Дождик их помыл немножко.  ');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (14,-345575051,'Солнце льётся на полянку, </br>Солнце яркое печёт. </br>На поляне земляника </br>Краснощёкая растёт! </br>Позову свою подружку. </br>Наберём мы ягод кружку! ');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (15,-345575051,'Вьётся в воздухе листва, </br>В жёлтых листьях вся Москва. </br>У окошка мы сидим </br>И глядим наружу. </br>Шепчут листья: - Улетим! - </br>И ныряют в лужу.');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (16,-345575051,'Вдоль болота ходит кто-то,</br>Травы поздние шуршат.</br>Это цапля у болота</br>Караулит лягушат.');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (17,-345575051,'Дятел, хоть и молодой, </br>Вежливая птица: </br>Он всегда перед едой</br>К червяку стучится.');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (18,-345575051,'Воробьи-воробушки, </br>Серенькие пёрышки! </br>Клюйте, клюйте крошки </br>У меня с ладошки!');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (19,-345575051,'По ночам сове не спится, </br>Ведь сова -</br>Ночная птица.</br>Настороженно и тихо,</br>При луне едва видна,</br>Как ночная сторожиха,</br>На сосне сидит она.');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (20,-345575051,'Кенгурёнка поутру</br>Моет мама кенгуру</br>Моет лапки, моет хвостик:</br>- Видишь, к нам шагают гости.');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (21,-345575051,'Петь задумал бегемот</br>И открыл огромный рот.</br>Но не слышно в песне слов,</br>Слышен только страшный рёв.');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (22,-345575051,'Льва увидишь - сразу ясно:</br>Царь зверей, шутить опасно.</br>Грозный вид и грозный рык,</br>Даже гривы не подстриг.');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (23,-345575051,'Не бывает так, чтоб нос</br>Рос не вниз, а вверх бы рос.</br>Лишь у зверя носорога</br>Вверх растёт он в виде рога.');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (24,-345575051,'У слона большие уши,</br>Как гора огромен слон.</br>Равных нет ему на суше:</br>Слон по весу - чемпион. ');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (25,-345575051,'Любит зебра по лужайке</br>В полосатой бегать майке.</br>Зебра даже за конфетку</br>Не наденет майку в клетку.');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (26,-345575051,'Очень любят обезьяны</br>Кушать сладкие бананы.</br>Мы на обезьян похожи,</br>И бананы любим тоже.');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (27,-345575051,'Я – лисичка, рыжий хвостик.</br>Я пришла к ребятам в гости.</br>Буду с ними веселиться,</br>Возле ёлочки кружиться.');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (28,-345575051,'К нам на ёлку, посмотри,</br>Прилетели снегири.</br>И уселись на макушке,</br>Как нарядные игрушки.');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (29,-345575051,'Лизнув сосульку, </br>Словно леденец,</br>Весна сказала: </br>- Всё! Зиме конец!');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (30,-345575051,'-Мишка, мишка! Что с тобой?</br>Почему ты спишь зимой?</br>-Потому что снег и лед-</br>Не малина и не мед!');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (31,-345575051,'Непривычны кошке</br>Нож и вилка с ложкой.</br>Ей бы прямо с блюдца</br>Съесть - и облизнуться.');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (32,-345575051,'Если б град был не из града,</br>А из ягод винограда,</br>Я б тогда под этот град</br>Без зонта ходить был рад!');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (33,-345575051,'Осень яркими цветами,</br>Всё раскрасило в саду,</br>Очень скоро за грибами,</br>Я с сестрёнкою пойду!');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (34,-345575051,'Тучка лопнула с утра,</br>Дождик льет - как из ведра.</br>Тучке надобен ремонт,</br>А тебе – хороший зонт');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (35,-345575051,'Яблоки растут на ветке.</br>Их пока не троньте, детки!</br>Вот созреет урожай –</br>Тут корзины подставляй!');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (36,-345575051,'В рыжем платьице из ситца</br>раскрасавица лисица.</br>Хвостик — с белым кончиком,</br>краска, что ли, кончилась? ');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (37,-345575051,'Почему меня сорвали</br>и томатом обозвали?</br>Что за шутки, что за вздор?</br>Я – весёлый помидор!');
INSERT INTO verse(_id, alphabet_id, verse_text) VALUES (38,-345575051,'Ель растёт в лесу густом.</br>Много рыжиков кругом.</br>Нагибается грибник –</br>Под ольхою боровик.');

/* 
***************************
word table
***************************
*/
-- For create words exerciseINSERT INTO word(_id, alphabet_id, word, complexity) VALUES (1,-345575051,'консервы',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (2,-345575051,'кон',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (3,-345575051,'нерв',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (4,-345575051,'сок',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (5,-345575051,'ров',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (6,-345575051,'вор',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (7,-345575051,'сор',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (8,-345575051,'вес',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (9,-345575051,'весы',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (10,-345575051,'сыр',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (11,-345575051,'нос',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (12,-345575051,'сон',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (13,-345575051,'рык',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (14,-345575051,'праздник',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (15,-345575051,'признак',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (16,-345575051,'задник',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (17,-345575051,'каприз',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (18,-345575051,'карниз',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (19,-345575051,'парник',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (20,-345575051,'приказ',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (21,-345575051,'кинза',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (22,-345575051,'кирза',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (23,-345575051,'парик',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (24,-345575051,'драп',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (25,-345575051,'знак',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (26,-345575051,'икра',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (27,-345575051,'карп',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (28,-345575051,'пика',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (29,-345575051,'пиар',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (30,-345575051,'парк',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (31,-345575051,'пари',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (32,-345575051,'зад',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (33,-345575051,'паз',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (34,-345575051,'пик',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (35,-345575051,'раз',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (36,-345575051,'ад',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (37,-345575051,'аз',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (38,-345575051,'периметр',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (39,-345575051,'перетир',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (40,-345575051,'репетир',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (41,-345575051,'триер',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (42,-345575051,'терем',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (43,-345575051,'темир',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (44,-345575051,'репер',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (45,-345575051,'метр',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (46,-345575051,'ритм',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (47,-345575051,'темп',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (48,-345575051,'тире',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (49,-345575051,'трип',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (50,-345575051,'мир',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (51,-345575051,'пир',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (52,-345575051,'тир',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (53,-345575051,'абстракция',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (54,-345575051,'кастрация',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (55,-345575051,'таксация',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (56,-345575051,'актриса',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (57,-345575051,'арабист',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (58,-345575051,'старица',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (59,-345575051,'баркас',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (60,-345575051,'батрак',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (61,-345575051,'карбас',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (62,-345575051,'ракита',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (63,-345575051,'сатира',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (64,-345575051,'старик',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (65,-345575051,'старка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (66,-345575051,'стирка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (67,-345575051,'тряска',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (68,-345575051,'акция',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (69,-345575051,'астра',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (70,-345575051,'баска',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (71,-345575051,'барак',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (72,-345575051,'бирка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (73,-345575051,'искра',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (74,-345575051,'карат',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (75,-345575051,'кариб',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (76,-345575051,'карта',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (77,-345575051,'киста',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (78,-345575051,'каста',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (79,-345575051,'краса',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (80,-345575051,'рация',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (81,-345575051,'ряска',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (82,-345575051,'табак',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (83,-345575051,'скраб',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (84,-345575051,'такса',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (85,-345575051,'такси',2);
--INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (86,-345575051,'аист',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (87,-345575051,'араб',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (88,-345575051,'ария',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (89,-345575051,'арка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (90,-345575051,'барс',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (91,-345575051,'брат',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (92,-345575051,'брас',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (93,-345575051,'бита',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (94,-345575051,'брак',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (95,-345575051,'кара',2);
--INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (96,-345575051,'краб',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (97,-345575051,'раса',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (98,-345575051,'риск',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (99,-345575051,'ряса',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (100,-345575051,'скат',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (101,-345575051,'скит',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (102,-345575051,'тара',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (103,-345575051,'стая',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (104,-345575051,'цирк',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (105,-345575051,'акт',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (106,-345575051,'бак',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (107,-345575051,'бит',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (108,-345575051,'бра',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (109,-345575051,'кит',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (110,-345575051,'кия',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (111,-345575051,'раб',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (112,-345575051,'тик',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (114,-345575051,'ас',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (115,-345575051,'як',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (116,-345575051,'авиамодель',5);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (117,-345575051,'авиадело',5);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (118,-345575051,'ведьма',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (119,-345575051,'модель',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (120,-345575051,'медаль',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (121,-345575051,'видео',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (122,-345575051,'идеал',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (123,-345575051,'молва',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (124,-345575051,'олива',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (125,-345575051,'вода',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (126,-345575051,'даль',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (127,-345575051,'дама',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (128,-345575051,'дева',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (129,-345575051,'дело',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (130,-345575051,'диво',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (131,-345575051,'леди',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (132,-345575051,'медь',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (133,-345575051,'мель',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (134,-345575051,'овал',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (135,-345575051,'моль',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (136,-345575051,'мода',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (137,-345575051,'вал',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (138,-345575051,'вид',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (139,-345575051,'дол',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (141,-345575051,'лье',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (143,-345575051,'австралиц',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (144,-345575051,'австриец',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (145,-345575051,'светлица',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (146,-345575051,'сталевар',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (147,-345575051,'царствие',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (148,-345575051,'атласец',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (149,-345575051,'левират',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (150,-345575051,'латвиец',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (151,-345575051,'литавра',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (152,-345575051,'расцвет',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (153,-345575051,'реалист',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (154,-345575051,'селитра',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (156,-345575051,'алтаец',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (157,-345575051,'верста',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (158,-345575051,'литера',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (159,-345575051,'листва',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (161,-345575051,'свитер',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (162,-345575051,'сериал',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (163,-345575051,'стрела',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (164,-345575051,'ставец',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (165,-345575051,'аврал',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (166,-345575051,'арест',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (168,-345575051,'атлас',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (169,-345575051,'валет',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (170,-345575051,'врата',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (171,-345575051,'лавра',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (172,-345575051,'истец',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (173,-345575051,'ларец',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (174,-345575051,'ливер',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (175,-345575051,'салат',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (176,-345575051,'свита',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (177,-345575051,'ситец',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (178,-345575051,'слава',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (179,-345575051,'трава',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (180,-345575051,'циста',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (182,-345575051,'атас',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (183,-345575051,'вата',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (184,-345575051,'вера',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (185,-345575051,'лава',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (186,-345575051,'лавр',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (187,-345575051,'ласт',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (188,-345575051,'лист',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (189,-345575051,'литр',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (191,-345575051,'сват',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (192,-345575051,'сила',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (193,-345575051,'сера',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (194,-345575051,'слет',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (195,-345575051,'автогонщик',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (196,-345575051,'вагонщик',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (197,-345575051,'отгонщик',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (198,-345575051,'готовка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (199,-345575051,'овощник',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (200,-345575051,'ватник',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (201,-345575051,'готика',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (202,-345575051,'гонщик',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (203,-345575051,'катион',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (204,-345575051,'огниво',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (205,-345575051,'актив',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (206,-345575051,'вагон',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (207,-345575051,'ватин',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (208,-345575051,'виток',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (209,-345575051,'гонка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (210,-345575051,'икона',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (211,-345575051,'икота',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (212,-345575051,'квант',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (213,-345575051,'квота',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (214,-345575051,'книга',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (215,-345575051,'нитка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (216,-345575051,'танго',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (217,-345575051,'щиток',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (218,-345575051,'тоник',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (219,-345575051,'винт',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (220,-345575051,'вино',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (221,-345575051,'воин',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (222,-345575051,'итог',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (223,-345575051,'кино',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (224,-345575051,'нога',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (225,-345575051,'овощ',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (226,-345575051,'окно',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (227,-345575051,'танк',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (228,-345575051,'тина',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (229,-345575051,'ива',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (232,-345575051,'око',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (234,-345575051,'тан',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (235,-345575051,'тон',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (236,-345575051,'ток',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (237,-345575051,'щит',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (238,-345575051,'акварель',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (239,-345575051,'кавальер',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (240,-345575051,'кавалер',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (241,-345575051,'лекарь',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (244,-345575051,'варка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (247,-345575051,'елка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (250,-345575051,'река',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (251,-345575051,'акр',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (252,-345575051,'ель',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (253,-345575051,'лев',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (254,-345575051,'лак',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (255,-345575051,'антарктика',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (256,-345575051,'картинка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (257,-345575051,'катаракт',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (258,-345575051,'антракт',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (259,-345575051,'кантата',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (260,-345575051,'картина',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (261,-345575051,'накатка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (262,-345575051,'натирка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (263,-345575051,'тактика',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (264,-345575051,'татарка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (265,-345575051,'татарин',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (266,-345575051,'таракан',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (267,-345575051,'иранка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (268,-345575051,'кантик',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (269,-345575051,'катран',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (270,-345575051,'нитрат',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (271,-345575051,'ратник',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (272,-345575051,'тактик',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (273,-345575051,'акант',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (274,-345575051,'акита',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (275,-345575051,'антик',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (276,-345575051,'таблетка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (277,-345575051,'атлетка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (278,-345575051,'балетка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (279,-345575051,'атлет',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (280,-345575051,'бакал',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (281,-345575051,'балет',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (282,-345575051,'балка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (283,-345575051,'катет',2);
--INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (284,-345575051,'белка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (286,-345575051,'блат',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (289,-345575051,'бал',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (290,-345575051,'кал',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (291,-345575051,'талисман',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (292,-345575051,'ламаист',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (293,-345575051,'маслина',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (294,-345575051,'ласина',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (295,-345575051,'малина',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (296,-345575051,'астма',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (298,-345575051,'лиман',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (299,-345575051,'лиана',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (300,-345575051,'ислам',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (301,-345575051,'сатин',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (304,-345575051,'тмин',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (306,-345575051,'мат',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (307,-345575051,'тактичность',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (308,-345575051,'отчистка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (309,-345575051,'ниточка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (310,-345575051,'останки',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (311,-345575051,'остатки',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (312,-345575051,'скотина',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (313,-345575051,'очистка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (314,-345575051,'танкист',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (315,-345575051,'частник',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (316,-345575051,'чистота',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (318,-345575051,'натиск',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (319,-345575051,'синька',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (320,-345575051,'станок',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (321,-345575051,'сотник',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (322,-345575051,'считка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (323,-345575051,'токсин',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (324,-345575051,'чистка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (327,-345575051,'исток',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (329,-345575051,'кисть',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (330,-345575051,'кость',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (331,-345575051,'качан',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (332,-345575051,'носка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (333,-345575051,'осина',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (334,-345575051,'сачок',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (335,-345575051,'откат',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (336,-345575051,'санки',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (337,-345575051,'сотка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (338,-345575051,'титан',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (339,-345575051,'часть',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (340,-345575051,'читка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (341,-345575051,'точка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (342,-345575051,'тоска',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (343,-345575051,'коса',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (344,-345575051,'скот',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (345,-345575051,'стан',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (347,-345575051,'сток',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (348,-345575051,'стон',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (349,-345575051,'правильность',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (350,-345575051,'прославить',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (351,-345575051,'плавность',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (352,-345575051,'впалость',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (353,-345575051,'павильон',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (354,-345575051,'пристань',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (355,-345575051,'стволина',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (356,-345575051,'лопасть',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (357,-345575051,'отрасль',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (358,-345575051,'плотина',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (359,-345575051,'правило',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (360,-345575051,'пристав',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (361,-345575051,'словарь',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (362,-345575051,'ваниль',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (363,-345575051,'павлин',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (364,-345575051,'пароль',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (365,-345575051,'патрон',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (366,-345575051,'пальто',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (367,-345575051,'сальто',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (368,-345575051,'автор',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (369,-345575051,'вальс',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (371,-345575051,'вольт',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (372,-345575051,'вопль',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (373,-345575051,'напор',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (374,-345575051,'пират',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (375,-345575051,'пилот',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (376,-345575051,'пинта',2);
--INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (377,-345575051,'сироп',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (378,-345575051,'спина',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (379,-345575051,'талон',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (380,-345575051,'стопа',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (381,-345575051,'вонь',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (383,-345575051,'пила',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (384,-345575051,'соль',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (385,-345575051,'сопа',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (386,-345575051,'торс',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (387,-345575051,'лис',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (389,-345575051,'пол',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (390,-345575051,'рот',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (392,-345575051,'запеканка',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (393,-345575051,'казанка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (394,-345575051,'пазанка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (395,-345575051,'капкан',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (396,-345575051,'казак',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (397,-345575051,'казан',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (398,-345575051,'казна',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (399,-345575051,'наказ',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (400,-345575051,'кепка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (401,-345575051,'пенка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (402,-345575051,'пена',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (403,-345575051,'космонавт',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (404,-345575051,'восток',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (405,-345575051,'котома',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (406,-345575051,'мосток',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (408,-345575051,'смотка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (411,-345575051,'моток',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (412,-345575051,'носок',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (414,-345575051,'кант',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (415,-345575051,'воск',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (416,-345575051,'атом',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (420,-345575051,'смак',1);
--INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (421,-345575051,'мак',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (422,-345575051,'ком',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (423,-345575051,'прабабушка',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (424,-345575051,'бабушка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (425,-345575051,'рубашка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (426,-345575051,'арабка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (427,-345575051,'абаша',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (428,-345575051,'бабка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (429,-345575051,'барка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (431,-345575051,'бурак',2);
--INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (432,-345575051,'шапка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (433,-345575051,'шкура',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (435,-345575051,'баба',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (438,-345575051,'паук',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (439,-345575051,'пара',2);
--INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (440,-345575051,'шуба',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (441,-345575051,'шар',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (442,-345575051,'пар',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (443,-345575051,'бук',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (444,-345575051,'бур',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (445,-345575051,'куш',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (446,-345575051,'рак',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (447,-345575051,'младенец',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (448,-345575051,'меледа',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (449,-345575051,'малец',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (450,-345575051,'немец',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (451,-345575051,'цена',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (452,-345575051,'еда',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (453,-345575051,'лад',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (454,-345575051,'лед',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (455,-345575051,'мегаполис',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (456,-345575051,'могила',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (457,-345575051,'галоп',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (458,-345575051,'масло',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (459,-345575051,'олимп',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (460,-345575051,'полис',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (461,-345575051,'сапог',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (462,-345575051,'смола',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (463,-345575051,'гипс',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (464,-345575051,'глас',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (465,-345575051,'лиса',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (466,-345575051,'лига',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (467,-345575051,'липа',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (468,-345575051,'мопс',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (469,-345575051,'осел',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (470,-345575051,'маг',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (471,-345575051,'лес',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (472,-345575051,'ил',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (473,-345575051,'медалистка',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (474,-345575051,'академист',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (475,-345575051,'медалист',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (476,-345575051,'дискета',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (477,-345575051,'мастика',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (478,-345575051,'сиделка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (479,-345575051,'ластик',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (480,-345575051,'латекс',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (481,-345575051,'климат',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (482,-345575051,'мастак',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (483,-345575051,'сделка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (485,-345575051,'макет',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (486,-345575051,'маска',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (487,-345575051,'смета',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (488,-345575051,'метла',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (491,-345575051,'аскет',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (492,-345575051,'матка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (493,-345575051,'секта',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (494,-345575051,'кадет',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (496,-345575051,'медик',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (497,-345575051,'миска',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (498,-345575051,'склад',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (500,-345575051,'самка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (501,-345575051,'сетка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (503,-345575051,'метка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (504,-345575051,'диета',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (505,-345575051,'календарь',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (506,-345575051,'кандела',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (508,-345575051,'кенарь',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (510,-345575051,'редька',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (511,-345575051,'наклад',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (512,-345575051,'аркан',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (513,-345575051,'арена',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (514,-345575051,'декан',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (515,-345575051,'дрель',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (516,-345575051,'кладь',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (517,-345575051,'ладан',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (518,-345575051,'накал',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (519,-345575051,'недра',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (520,-345575051,'кедр',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (521,-345575051,'клад',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (522,-345575051,'клан',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (523,-345575051,'клен',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (524,-345575051,'кран',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (525,-345575051,'лень',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (526,-345575051,'рань',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (528,-345575051,'ара',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (530,-345575051,'дар',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (532,-345575051,'песочница',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (533,-345575051,'испанец',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (535,-345575051,'писец',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (536,-345575051,'сцена',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (538,-345575051,'оспа',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (539,-345575051,'пони',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (540,-345575051,'сани',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (541,-345575051,'сено',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (544,-345575051,'час',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (545,-345575051,'ион',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (546,-345575051,'растение',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (547,-345575051,'старение',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (548,-345575051,'интерес',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (549,-345575051,'нерест',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (550,-345575051,'сирена',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (551,-345575051,'трение',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (553,-345575051,'рента',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (554,-345575051,'сенат',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (555,-345575051,'тиран',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (557,-345575051,'стена',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (558,-345575051,'транс',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (560,-345575051,'рис',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (561,-345575051,'разговор',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (562,-345575051,'заговор',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (563,-345575051,'говор',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (564,-345575051,'овраг',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (565,-345575051,'рогоз',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (566,-345575051,'розга',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (567,-345575051,'гора',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (568,-345575051,'взор',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (569,-345575051,'роза',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (570,-345575051,'газ',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (572,-345575051,'рог',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (573,-345575051,'разновес',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (574,-345575051,'развес',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (575,-345575051,'зарево',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (576,-345575051,'невроз',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (577,-345575051,'разнос',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (578,-345575051,'занос',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (579,-345575051,'взнос',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (580,-345575051,'весна',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (581,-345575051,'засов',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (582,-345575051,'засев',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (583,-345575051,'навоз',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (584,-345575051,'резон',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (585,-345575051,'сезон',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (586,-345575051,'вена',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (587,-345575051,'овес',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (588,-345575051,'овен',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (589,-345575051,'нрав',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (591,-345575051,'роса',2);
--INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (592,-345575051,'сова',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (593,-345575051,'своз',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (594,-345575051,'срез',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (596,-345575051,'воз',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (597,-345575051,'наследник',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (598,-345575051,'индекс',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (599,-345575051,'лесник',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (600,-345575051,'ледник',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (601,-345575051,'десна',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (602,-345575051,'длина',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (603,-345575051,'леска',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (604,-345575051,'надел',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (607,-345575051,'навигатор',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (608,-345575051,'аргонавт',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (609,-345575051,'рогатина',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (610,-345575051,'авиатор',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (611,-345575051,'вариант',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (612,-345575051,'гарант',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (613,-345575051,'гранат',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (614,-345575051,'гранит',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (615,-345575051,'гривна',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (616,-345575051,'нагота',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (617,-345575051,'варан',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (618,-345575051,'грива',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (619,-345575051,'навар',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (620,-345575051,'орган',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (621,-345575051,'рвота',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (624,-345575051,'товар',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (629,-345575051,'трон',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (630,-345575051,'рота',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (631,-345575051,'надежность',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (632,-345575051,'данность',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (633,-345575051,'жадность',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (634,-345575051,'нежность',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (635,-345575051,'десант',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (636,-345575051,'сажень',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (637,-345575051,'тоннаж',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (639,-345575051,'досье',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (640,-345575051,'жетон',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (641,-345575051,'жесть',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (642,-345575051,'сонет',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (643,-345575051,'стенд',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (644,-345575051,'тонна',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (645,-345575051,'день',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (646,-345575051,'дань',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (647,-345575051,'наст',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (649,-345575051,'стаж',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (650,-345575051,'сеть',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (651,-345575051,'нож',1);


----------------------------------- 652+

INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (652,-345575051,'аист',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (653,-345575051,'лошадь',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (654,-345575051,'сова',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (655,-345575051,'белка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (656,-345575051,'бабочка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (657,-345575051,'гриб',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (658,-345575051,'волк',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (659,-345575051,'корова',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (660,-345575051,'остров',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (661,-345575051,'гном',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (662,-345575051,'бегемот',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (663,-345575051,'утюг',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (664,-345575051,'дом',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (665,-345575051,'радуга',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (666,-345575051,'след',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (667,-345575051,'енот',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (668,-345575051,'змея',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (669,-345575051,'пирожное',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (670,-345575051,'ёж',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (671,-345575051,'мёд',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (672,-345575051,'ружьё',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (673,-345575051,'жаба',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (674,-345575051,'морж',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (675,-345575051,'мороженое',5);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (676,-345575051,'зебра',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (677,-345575051,'гнездо',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (678,-345575051,'арбуз',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (679,-345575051,'индюк',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (680,-345575051,'машина',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (681,-345575051,'нитки',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (682,-345575051,'йогурт',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (683,-345575051,'балалайка',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (684,-345575051,'воробей',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (685,-345575051,'краб',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (686,-345575051,'морковь',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (687,-345575051,'мак',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (688,-345575051,'ласточка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (689,-345575051,'апельсин',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (690,-345575051,'козёл',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (691,-345575051,'медведь',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (692,-345575051,'комар',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (693,-345575051,'сом',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (694,-345575051,'носорог',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (695,-345575051,'солнце',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (696,-345575051,'барабан',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (697,-345575051,'обезьяна',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (698,-345575051,'бобер',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (699,-345575051,'зерно',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (700,-345575051,'пингвин',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (701,-345575051,'шапка',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (702,-345575051,'сироп',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (703,-345575051,'ромашка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (704,-345575051,'торт',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (705,-345575051,'клевер',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (706,-345575051,'собака',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (707,-345575051,'костёр',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (708,-345575051,'компас',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (709,-345575051,'телевизор',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (710,-345575051,'стул',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (711,-345575051,'кот',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (712,-345575051,'улей',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (713,-345575051,'кукуруза',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (714,-345575051,'кенгуру',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (715,-345575051,'фонтан',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (716,-345575051,'конФеты',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (717,-345575051,'жираф',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (718,-345575051,'хлеб',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (719,-345575051,'пароход',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (720,-345575051,'подсолнух',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (721,-345575051,'цыпленок',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (722,-345575051,'курица',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (723,-345575051,'заяц',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (724,-345575051,'черепаха',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (725,-345575051,'дача',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (726,-345575051,'мяч',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (727,-345575051,'шуба',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (728,-345575051,'чаша',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (729,-345575051,'ёрш',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (730,-345575051,'щегол',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (731,-345575051,'сгущенка',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (732,-345575051,'плащ',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (733,-345575051,'объятие',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (734,-345575051,'мышь',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (735,-345575051,'шары',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (736,-345575051,'колокольчики',5);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (737,-345575051,'шмель',1);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (738,-345575051,'эскаватор',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (739,-345575051,'аэропорт',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (740,-345575051,'алоэ',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (741,-345575051,'юла',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (742,-345575051,'дюймовочка',4);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (743,-345575051,'меню',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (744,-345575051,'яблоко',3);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (745,-345575051,'месяц',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (746,-345575051,'семья',2);
INSERT INTO word(_id, alphabet_id, word, complexity) VALUES (747,-345575051,'бас',1);

/* 
***************************
word_image_description table
***************************
*/
INSERT INTO word_image_description(word_id, image_id) VALUES (652,5);
INSERT INTO word_image_description(word_id, image_id) VALUES (653,3);
INSERT INTO word_image_description(word_id, image_id) VALUES (654,4);
INSERT INTO word_image_description(word_id, image_id) VALUES (655,10);
INSERT INTO word_image_description(word_id, image_id) VALUES (656,8);
INSERT INTO word_image_description(word_id, image_id) VALUES (657,9);
INSERT INTO word_image_description(word_id, image_id) VALUES (658,15);
INSERT INTO word_image_description(word_id, image_id) VALUES (659,13);
INSERT INTO word_image_description(word_id, image_id) VALUES (660,14);
INSERT INTO word_image_description(word_id, image_id) VALUES (661,18);
INSERT INTO word_image_description(word_id, image_id) VALUES (662,19);
INSERT INTO word_image_description(word_id, image_id) VALUES (663,20);
INSERT INTO word_image_description(word_id, image_id) VALUES (664,23);
INSERT INTO word_image_description(word_id, image_id) VALUES (665,24);
INSERT INTO word_image_description(word_id, image_id) VALUES (666,25);
INSERT INTO word_image_description(word_id, image_id) VALUES (667,29);
INSERT INTO word_image_description(word_id, image_id) VALUES (668,30);
INSERT INTO word_image_description(word_id, image_id) VALUES (669,28);
INSERT INTO word_image_description(word_id, image_id) VALUES (670,34);
INSERT INTO word_image_description(word_id, image_id) VALUES (671,35);
INSERT INTO word_image_description(word_id, image_id) VALUES (672,33);
INSERT INTO word_image_description(word_id, image_id) VALUES (673,39);
INSERT INTO word_image_description(word_id, image_id) VALUES (674,40);
INSERT INTO word_image_description(word_id, image_id) VALUES (675,38);
INSERT INTO word_image_description(word_id, image_id) VALUES (676,45);
INSERT INTO word_image_description(word_id, image_id) VALUES (677,43);
INSERT INTO word_image_description(word_id, image_id) VALUES (678,44);
INSERT INTO word_image_description(word_id, image_id) VALUES (679,50);
INSERT INTO word_image_description(word_id, image_id) VALUES (680,48);
INSERT INTO word_image_description(word_id, image_id) VALUES (681,49);
INSERT INTO word_image_description(word_id, image_id) VALUES (682,55);
INSERT INTO word_image_description(word_id, image_id) VALUES (683,53);
INSERT INTO word_image_description(word_id, image_id) VALUES (684,54);
INSERT INTO word_image_description(word_id, image_id) VALUES (685,59);
INSERT INTO word_image_description(word_id, image_id) VALUES (686,58);
INSERT INTO word_image_description(word_id, image_id) VALUES (687,60);
INSERT INTO word_image_description(word_id, image_id) VALUES (688,65);
INSERT INTO word_image_description(word_id, image_id) VALUES (689,64);
INSERT INTO word_image_description(word_id, image_id) VALUES (690,63);
INSERT INTO word_image_description(word_id, image_id) VALUES (691,68);
INSERT INTO word_image_description(word_id, image_id) VALUES (692,70);
INSERT INTO word_image_description(word_id, image_id) VALUES (693,69);
INSERT INTO word_image_description(word_id, image_id) VALUES (694,73);
INSERT INTO word_image_description(word_id, image_id) VALUES (695,74);
INSERT INTO word_image_description(word_id, image_id) VALUES (696,75);
INSERT INTO word_image_description(word_id, image_id) VALUES (697,80);
INSERT INTO word_image_description(word_id, image_id) VALUES (698,78);
INSERT INTO word_image_description(word_id, image_id) VALUES (699,79);
INSERT INTO word_image_description(word_id, image_id) VALUES (700,85);
INSERT INTO word_image_description(word_id, image_id) VALUES (701,83);
INSERT INTO word_image_description(word_id, image_id) VALUES (702,84);
INSERT INTO word_image_description(word_id, image_id) VALUES (703,89);
INSERT INTO word_image_description(word_id, image_id) VALUES (704,88);
INSERT INTO word_image_description(word_id, image_id) VALUES (705,90);
INSERT INTO word_image_description(word_id, image_id) VALUES (706,95);
INSERT INTO word_image_description(word_id, image_id) VALUES (707,93);
INSERT INTO word_image_description(word_id, image_id) VALUES (708,94);
INSERT INTO word_image_description(word_id, image_id) VALUES (709,100);
INSERT INTO word_image_description(word_id, image_id) VALUES (710,99);
INSERT INTO word_image_description(word_id, image_id) VALUES (711,98);
INSERT INTO word_image_description(word_id, image_id) VALUES (712,103);
INSERT INTO word_image_description(word_id, image_id) VALUES (713,104);
INSERT INTO word_image_description(word_id, image_id) VALUES (714,105);
INSERT INTO word_image_description(word_id, image_id) VALUES (715,109);
INSERT INTO word_image_description(word_id, image_id) VALUES (716,108);
INSERT INTO word_image_description(word_id, image_id) VALUES (717,110);
INSERT INTO word_image_description(word_id, image_id) VALUES (718,113);
INSERT INTO word_image_description(word_id, image_id) VALUES (719,114);
INSERT INTO word_image_description(word_id, image_id) VALUES (720,115);
INSERT INTO word_image_description(word_id, image_id) VALUES (721,118);
INSERT INTO word_image_description(word_id, image_id) VALUES (722,120);
INSERT INTO word_image_description(word_id, image_id) VALUES (723,119);
INSERT INTO word_image_description(word_id, image_id) VALUES (724,125);
INSERT INTO word_image_description(word_id, image_id) VALUES (725,124);
INSERT INTO word_image_description(word_id, image_id) VALUES (726,123);
INSERT INTO word_image_description(word_id, image_id) VALUES (727,129);
INSERT INTO word_image_description(word_id, image_id) VALUES (728,128);
INSERT INTO word_image_description(word_id, image_id) VALUES (729,130);
INSERT INTO word_image_description(word_id, image_id) VALUES (730,135);
INSERT INTO word_image_description(word_id, image_id) VALUES (731,134);
INSERT INTO word_image_description(word_id, image_id) VALUES (732,133);
INSERT INTO word_image_description(word_id, image_id) VALUES (733,138);
INSERT INTO word_image_description(word_id, image_id) VALUES (734,142);
INSERT INTO word_image_description(word_id, image_id) VALUES (735,141);
INSERT INTO word_image_description(word_id, image_id) VALUES (736,145);
INSERT INTO word_image_description(word_id, image_id) VALUES (737,146);
INSERT INTO word_image_description(word_id, image_id) VALUES (738,151);
INSERT INTO word_image_description(word_id, image_id) VALUES (739,149);
INSERT INTO word_image_description(word_id, image_id) VALUES (740,150);
INSERT INTO word_image_description(word_id, image_id) VALUES (741,156);
INSERT INTO word_image_description(word_id, image_id) VALUES (742,155);
INSERT INTO word_image_description(word_id, image_id) VALUES (743,154);
INSERT INTO word_image_description(word_id, image_id) VALUES (744,159);
INSERT INTO word_image_description(word_id, image_id) VALUES (745,161);
INSERT INTO word_image_description(word_id, image_id) VALUES (746,160);

/* 
***************************
word_sound_description table
***************************
*/
INSERT INTO word_sound_description(word_id, sound_id) VALUES(28, 1);
INSERT INTO word_sound_description(word_id, sound_id) VALUES(29, 1);
INSERT INTO word_sound_description(word_id, sound_id) VALUES(30, 1);
INSERT INTO word_sound_description(word_id, sound_id) VALUES(31, 1);
INSERT INTO word_sound_description(word_id, sound_id) VALUES(32, 1);
INSERT INTO word_sound_description(word_id, sound_id) VALUES(33, 1);

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
INSERT INTO sound_words(character_exercise_id, word_id, sound_flag) VALUES(1, 28, 1);
INSERT INTO sound_words(character_exercise_id, word_id, sound_flag) VALUES(1, 29, 3);
INSERT INTO sound_words(character_exercise_id, word_id, sound_flag) VALUES(1, 30, 5);
INSERT INTO sound_words(character_exercise_id, word_id, sound_flag) VALUES(1, 31, 5);
INSERT INTO sound_words(character_exercise_id, word_id, sound_flag) VALUES(1, 32, 5);
INSERT INTO sound_words(character_exercise_id, word_id, sound_flag) VALUES(1, 33, 5);

/* 
***************************
word_creation_exercise table
***************************
*/
INSERT INTO word_creation_exercise(word_id) VALUES (14);
INSERT INTO word_creation_exercise(word_id) VALUES (38);
INSERT INTO word_creation_exercise(word_id) VALUES (53);
INSERT INTO word_creation_exercise(word_id) VALUES (54);
INSERT INTO word_creation_exercise(word_id) VALUES (55);
INSERT INTO word_creation_exercise(word_id) VALUES (116);
INSERT INTO word_creation_exercise(word_id) VALUES (117);
INSERT INTO word_creation_exercise(word_id) VALUES (143);
INSERT INTO word_creation_exercise(word_id) VALUES (144);
INSERT INTO word_creation_exercise(word_id) VALUES (145);
INSERT INTO word_creation_exercise(word_id) VALUES (146);
INSERT INTO word_creation_exercise(word_id) VALUES (147);
INSERT INTO word_creation_exercise(word_id) VALUES (195);
INSERT INTO word_creation_exercise(word_id) VALUES (196);
INSERT INTO word_creation_exercise(word_id) VALUES (197);
INSERT INTO word_creation_exercise(word_id) VALUES (238);
INSERT INTO word_creation_exercise(word_id) VALUES (239);
INSERT INTO word_creation_exercise(word_id) VALUES (255);
INSERT INTO word_creation_exercise(word_id) VALUES (256);
INSERT INTO word_creation_exercise(word_id) VALUES (257);
INSERT INTO word_creation_exercise(word_id) VALUES (276);
INSERT INTO word_creation_exercise(word_id) VALUES (291);
INSERT INTO word_creation_exercise(word_id) VALUES (307);
INSERT INTO word_creation_exercise(word_id) VALUES (308);
INSERT INTO word_creation_exercise(word_id) VALUES (349);
INSERT INTO word_creation_exercise(word_id) VALUES (350);
INSERT INTO word_creation_exercise(word_id) VALUES (351);
INSERT INTO word_creation_exercise(word_id) VALUES (352);
INSERT INTO word_creation_exercise(word_id) VALUES (353);
INSERT INTO word_creation_exercise(word_id) VALUES (354);
INSERT INTO word_creation_exercise(word_id) VALUES (355);
INSERT INTO word_creation_exercise(word_id) VALUES (392);
INSERT INTO word_creation_exercise(word_id) VALUES (403);
INSERT INTO word_creation_exercise(word_id) VALUES (423);
INSERT INTO word_creation_exercise(word_id) VALUES (447);
INSERT INTO word_creation_exercise(word_id) VALUES (455);
INSERT INTO word_creation_exercise(word_id) VALUES (473);
INSERT INTO word_creation_exercise(word_id) VALUES (474);
INSERT INTO word_creation_exercise(word_id) VALUES (475);
INSERT INTO word_creation_exercise(word_id) VALUES (505);
INSERT INTO word_creation_exercise(word_id) VALUES (532);
INSERT INTO word_creation_exercise(word_id) VALUES (546);
INSERT INTO word_creation_exercise(word_id) VALUES (547);
INSERT INTO word_creation_exercise(word_id) VALUES (561);
INSERT INTO word_creation_exercise(word_id) VALUES (573);
INSERT INTO word_creation_exercise(word_id) VALUES (597);
INSERT INTO word_creation_exercise(word_id) VALUES (607);
INSERT INTO word_creation_exercise(word_id) VALUES (608);
INSERT INTO word_creation_exercise(word_id) VALUES (609);
INSERT INTO word_creation_exercise(word_id) VALUES (631);
INSERT INTO word_creation_exercise(word_id) VALUES (632);
INSERT INTO word_creation_exercise(word_id) VALUES (633);
INSERT INTO word_creation_exercise(word_id) VALUES (634);
