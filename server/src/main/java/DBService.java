import java.sql.*;
import java.util.Date;

public class DBService {
    private static Connection connection;
    private static Statement stmt;

    public static void connect(){
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            stmt = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static String getNickByLoginAndPass(String login, String pass) {
        String sql = String.format("select nickname from users where login = '%s' and password = '%s'", login, pass.hashCode());
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  null;
    }
    public static void addToBlackList(String userNick, String blacklistNick) {

        try {
            String  user_id = getIdByNickname(userNick);
            String bl_user_id = getIdByNickname(blacklistNick);

            String query = "INSERT INTO blacklist (user_id ,bl_user_id) VALUES (?, ?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, Integer.parseInt(user_id));
            ps.setInt(2, Integer.parseInt(bl_user_id));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getIdByNickname(String userNick) {
        String sql = String.format("select id from users where nickname = '%s'", userNick);
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  null;
    }


    public static void disconnect(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static boolean checkClient(String nickname){
        String sql = String.format("select nickname from users");
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                if(rs.getString(1).equals(nickname)) return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  false;
    }

    public static String isInBlacklist(ClientHandler client, String str) {
        String tockens[] = str.split(" ");
        StringBuilder sb = new StringBuilder();
        sb.append("/clientslist ");
        for(int i = 1; i < tockens.length; i++){
            if(getBlackList(client.getNick(), tockens[i]) != null) {
                System.out.println(getBlackList(client.getNick(), tockens[i]));
                sb.append(getBlackList(client.getNick(), tockens[i]) + "_blocked ");
            }else sb.append(tockens[i] + " ");
        }
        String out = sb.toString();
        return  out;
    }

    public static String getBlackList(String userNick, String bl_nick) {
        String nick1 = getIdByNickname(userNick);
        String sql = String.format("select nickname from blacklist\n" +
                "inner join users on bl_user_id = users.id and user_id ='%s'", nick1);
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                if(rs.getString(1).equals(bl_nick)) return rs.getString(1);
            }
                return null;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public static void regNewClient(String nickname, String login, String password) {
        try {
            String query = "INSERT INTO users (nickname ,login, password) VALUES (?, ?, ?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, nickname);
            ps.setString(2, login);
            ps.setInt(3, password.hashCode());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void logger(String nick, String action) {
        try {
            String query = "INSERT INTO logger (user_nickname ,action , action_date) VALUES (?, ?, ?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, nick);
            ps.setString(2, action);
            ps.setString(3, new Date().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
