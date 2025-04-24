package org.example.model;

import org.example.factory.ConnectionFactory;
import org.example.service.Autenticador;
import org.example.service.ConsultaCriptoativo;
import org.example.service.Corretora;
import org.example.exception.CorretoraException;
// ... imports java.sql.* etc ...
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// ... existing code ... 