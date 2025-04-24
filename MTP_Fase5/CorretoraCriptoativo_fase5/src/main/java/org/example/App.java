package org.example;

import org.example.ui.Main;
import org.example.factory.ConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class App
{
    public static void main( String[] args )
    {
       try {
            // Teste de conexão com o banco de dados
            Connection conexao = ConnectionFactory.getConnection();
            System.out.println("Conexão com o banco de dados realizada com sucesso!");
            conexao.close();
            
            // Executar a classe Main
            Main.main(args);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
