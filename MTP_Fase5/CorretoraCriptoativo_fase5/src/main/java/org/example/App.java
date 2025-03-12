package org.example;

import org.example.classes.MissaoTioPatinhas.src.Main;
import org.example.factory.ConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class App
{
    public static void main( String[] args )
    {
       try {
            Connection conexao = ConnectionFactory.getConnection();
            System.out.println("Conexão realizada!");
            
            // Executar a classe Main
            Main.main(args);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
