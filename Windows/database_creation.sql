CREATE TABLE special_sound (
	id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
        sound_id INTEGER NOT NULL,
        sound_type INTEGER NOT NULL,
        FOREIGN KEY(sound_id) REFERENCES sound(id),
        UNIQUE (sound_id) ON CONFLICT FAIL);

CREATE TABLE character_exercise (
            id INTEGER PRIMARY KEY,
            exercise_id INTEGER NOT NULL,
            character TEXT NOT NULL,
            FOREIGN KEY (exercise_id) REFERENCES exercise(id),
            UNIQUE (exercise_id) ON CONFLICT FAIL);

CREATE TABLE character_theory (
            id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
            character_exercise_id INTEGER NOT NULL,
            step_number INTEGER NOT NULL,
            image_id INTEGER,
            sound_id INTEGER,
            FOREIGN KEY(character_exercise_id) REFERENCES character_exercise(id),
            FOREIGN KEY(image_id) REFERENCES image(id),
            FOREIGN KEY(sound_id) REFERENCES sound(id));

CREATE TABLE character_verse (
            id INTEGER PRIMARY KEY,
            character_exercise_id INTEGER NOT NULL,
            verse TEXT NOT NULL,
            FOREIGN KEY(character_exercise_id) REFERENCES character_exercise(id), 
            UNIQUE (character_exercise_id, verse) ON CONFLICT FAIL);

CREATE TABLE character_object_image (
            id INTEGER PRIMARY KEY ASC AUTOINCREMENT, 
            character_exercise_id INTEGER NOT NULL, 
            word_description_id INTEGER NOT NULL,
            FOREIGN KEY(word_description_id) REFERENCES word_description(id),
            FOREIGN KEY(character_exercise_id) REFERENCES character_exercise(id),
            UNIQUE (character_exercise_id, word_description_id) ON CONFLICT FAIL);

CREATE TABLE word_description (
            id INTEGER PRIMARY KEY ASC AUTOINCREMENT,
            alphabet_id INTEGER NOT NULL,
            image_id INTEGER NOT NULL,
            object_name TEXT NOT NULL,
            UNIQUE (image_id, object_name) ON CONFLICT FAIL);

INSERT INTO character_exercise(exercise_id, character) VALUES (699588759, 'à');
INSERT INTO character_exercise(exercise_id, character) VALUES (-1329876691, 'á');
