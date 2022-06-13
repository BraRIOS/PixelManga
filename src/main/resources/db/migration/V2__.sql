INSERT INTO `roles` (`name`) VALUES ('USER');
INSERT INTO `roles` (`name`) VALUES ('AUTHOR');
INSERT INTO `roles` (`name`) VALUES ('ADMIN');

INSERT INTO `users` (`user_name`, `email`, `password`,`born_year`) VALUES ('admin', 'admin@admin','$2a$12$xl0BjbLkyUkwmkr9.m8bNOfGTSxQFU08v66qzYDhWn.LQdjOH2NN.', 0000);

INSERT INTO `users_roles` (`user_id`, `role_id`) VALUES (1, 3);

INSERT INTO `types` (`name`) VALUES ('tipo de libro');
INSERT INTO `types` (`name`) VALUES ('género');
INSERT INTO `types` (`name`) VALUES ('demografía');

INSERT INTO `attributes` (`name` , `type_id`) VALUES ('manga', 1);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('manwha', 1);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('manhua', 1);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('novela', 1);

INSERT INTO `attributes` (`name` , `type_id`) VALUES ('shounen', 3);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('shoujo', 3);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('seinen', 3);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('josei', 3);

INSERT INTO `attributes` (`name` , `type_id`) VALUES ('acción', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('apocalíptico', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('comedia', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('drama', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('recuentos de la vida', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('fantasía', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('ecchi', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('magia', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('ciencia ficción', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('sobrenatural', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('horror', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('deporte', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('yaoi', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('yuri', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('harem', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('mecha', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('supervivencia', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('reencarnación', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('isekai', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('gore', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('superpoderes', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('artes marciales', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('tragedia', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('militar', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('crimen', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('vampiros', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('samurái', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('cambio de género', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('realidad virtual', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('ciberpunk', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('música', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('parodia', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('demonios', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('familia', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('extranjero', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('niños', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('guerra', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('historia', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('misterio', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('policiaca', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('psicológico', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('romance', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('suspenso', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('thriller', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('vida escolar', 2);
INSERT INTO `attributes` (`name` , `type_id`) VALUES ('vida real', 2);