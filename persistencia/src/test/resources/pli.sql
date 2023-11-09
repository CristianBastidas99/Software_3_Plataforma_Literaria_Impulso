-- genero
INSERT INTO genero (id, nombre) VALUES
(1, 'Ficción'),
(2, 'Romance'),
(3, 'Ciencia Ficción'),
(4, 'Misterio'),
(5, 'Aventura');

-- administrador
INSERT INTO administrador (id, email, estado, nombre, password) VALUES
  (1, 'admin1@example.com', 'ACTIVO', 'Admin Uno', 'password123'),
  (2, 'admin2@example.com', 'ACTIVO', 'Admin Dos', 'securepass'),
  (3, 'admin3@example.com', 'INACTIVO', 'Admin Tres', 'adminpass'),
  (4, 'admin4@example.com', 'ACTIVO', 'Admin Cuatro', 'p@ssw0rd'),
  (5, 'admin5@example.com', 'INACTIVO', 'Admin Cinco', 'adminadmin');

-- lector
INSERT INTO lector (id, email, estado, nombre, password) VALUES
(1, 'reader1@example.com', 'ACTIVO', 'Lector Uno', 'readerpass'),
(2, 'reader2@example.com', 'ACTIVO', 'Lector Dos', 'bookslover'),
(3, 'reader3@example.com', 'INACTIVO', 'Lector Tres', 'readingnow'),
(4, 'reader4@example.com', 'ACTIVO', 'Lector Cuatro', 'pageturner'),
(5, 'reader5@example.com', 'INACTIVO', 'Lector Cinco', 'bookworm');

-- escritor
INSERT INTO escritor (id, email, estado, nombre, password, biografia) VALUES
(1, 'writer1@example.com', 'ACTIVO', 'Escritor Uno', 'writerpass', 'Autor de bestsellers'),
(2, 'writer2@example.com', 'ACTIVO', 'Escritor Dos', 'novelist123', 'Amante de la poesía'),
(3, 'writer3@example.com', 'INACTIVO', 'Escritor Tres', 'passforwriter', 'Escribo ciencia ficción'),
(4, 'writer4@example.com', 'ACTIVO', 'Escritor Cuatro', 'secretwriter', 'Ganador de premios literarios'),
(5, 'writer5@example.com', 'INACTIVO', 'Escritor Cinco', 'writerpass123', 'Autor independiente');

-- obra_literaria ACTIVO, INACTIVO, APROBADO, RECHAZADO, PENDIENTE, PUBLICADO
INSERT INTO obra_literaria (id, editorial, estado, fecha_publicacion, isbn, sinopsis, titulo) VALUES
(1, 'Editorial Uno', 'PUBLICADO', '2022-01-01', '1234567890', 'Una historia fascinante', 'Obra Uno'),
(2, 'Editorial Dos', 'PENDIENTE', '2022-02-01', '0987654321', 'Romance apasionado', 'Obra Dos'),
(3, 'Editorial Tres', 'PUBLICADO', '2022-03-01', '1357924680', 'Viaje intergaláctico', 'Obra Tres'),
(4, 'Editorial Cuatro', 'PENDIENTE', '2022-04-01', '2468013579', 'Intriga y suspenso', 'Obra Cuatro'),
(5, 'Editorial Cinco', 'PUBLICADO', '2022-05-01', '9876543210', 'Aventuras emocionantes', 'Obra Cinco');

-- escritor_obra_literaria
INSERT INTO escritor_obra_literaria (obra_literaria_id, escritor_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5);

-- lector_escritor_favorito
INSERT INTO lector_escritor_favorito (lector_id, escritor_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5);

-- obra_literaria_genero
INSERT INTO obra_literaria_genero (obra_literaria_id, genero_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5);

-- fragmento ACTIVO, INACTIVO, APROBADO, RECHAZADO, PENDIENTE, PUBLICADO
INSERT INTO fragmento (id, estado, url, obra_literaria_id) VALUES
(1, 'APROBADO', 'www.fragmento1.com', 1),
(2, 'PENDIENTE', 'www.fragmento2.com', 2),
(3, 'APROBADO', 'www.fragmento3.com', 3),
(4, 'PENDIENTE', 'www.fragmento4.com', 4),
(5, 'APROBADO', 'www.fragmento5.com', 5);

-- lector_genero_preferido
INSERT INTO lector_genero_preferido (lector_id, genero_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5);

-- publicacion ACTIVO, INACTIVO, APROBADO, RECHAZADO, PENDIENTE, PUBLICADO
INSERT INTO publicacion (id, contenido, estado, fecha_publicacion, titulo, url_imagen, obra_literaria_id) VALUES
(1, 'Contenido de la publicación Uno', 'PUBLICADO', '2023-11-09', 'Publicación Uno', 'www.imagen1.com', 1),
(2, 'Contenido de la publicación Dos', 'RECHAZADO', '2023-11-10', 'Publicación Dos', 'www.imagen2.com', 2),
(3, 'Contenido de la publicación Tres', 'PUBLICADO', '2023-11-11', 'Publicación Tres', 'www.imagen3.com', 3),
(4, 'Contenido de la publicación Cuatro', 'RECHAZADO', '2023-11-12', 'Publicación Cuatro', 'www.imagen4.com', 4),
(5, 'Contenido de la publicación Cinco', 'PUBLICADO', '2023-11-13', 'Publicación Cinco', 'www.imagen5.com', 5);

-- comentario ACTIVO, INACTIVO, APROBADO, RECHAZADO, PENDIENTE, PUBLICADO
INSERT INTO comentario (id, contenido, estado, fecha_publicacion, escritor_id, lector_id, publicacion_id) VALUES
(1, 'Excelente artículo', 'APROBADO', '2023-11-09 19:15:00', 1, 2, 1),
(2, 'Interesante punto de vista', 'RECHAZADO', '2023-11-09 19:20:00', 2, 3, 2),
(3, 'Muy informativo', 'APROBADO', '2023-11-09 19:30:00', 3, 4, 3),
(4, 'No estoy de acuerdo', 'RECHAZADO', '2023-11-09 19:45:00', 4, 5, 1),
(5, 'Me encantó la historia', 'APROBADO', '2023-11-09 20:00:00', 5, 1, 2);

-- lector_publicacion_vista
INSERT INTO lector_publicacion_vista (lector_id, publicacion_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5);

-- lector_publicacion_recomendada
INSERT INTO lector_publicacion_recomendada (lector_id, publicacion_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5);


