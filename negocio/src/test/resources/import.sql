-- Hibernate ejecuta este archivo al crear el esquema de pruebas (ddl-auto=create-drop).
-- Avanza la secuencia por encima de los ids fijos de pli.sql (1 a 5) para que las entidades
-- creadas en las pruebas no choquen con ellos. Se hace aquí, y no en pli.sql, porque pli.sql
-- corre dentro de la transacción de cada prueba y bloquearía la fila de hibernate_sequence.
UPDATE hibernate_sequence SET next_val = 1000;
