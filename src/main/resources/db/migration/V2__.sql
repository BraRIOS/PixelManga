INSERT INTO `roles` (`name`) VALUES ('USER');
INSERT INTO `roles` (`name`) VALUES ('AUTHOR');
INSERT INTO `roles` (`name`) VALUES ('ADMIN');

INSERT INTO `users` (`user_name`, `email`, `password`,`born_year`) VALUES ('admin', 'admin@admin','$2a$12$xl0BjbLkyUkwmkr9.m8bNOfGTSxQFU08v66qzYDhWn.LQdjOH2NN.', 0000);

INSERT INTO `users_roles` (`user_id`, `role_id`) VALUES (1, 3);