import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ClientHandler {
    private Socket socket;
    DataInputStream in;
    DataOutputStream out;
    MainServ serv;
    String nick;
    static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());


    public String getNick() {return nick;}


    public ClientHandler(MainServ serv, Socket socket){
    try {
        this.socket = socket;
        this.serv = serv;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    while (true) {
                        String msg = in.readUTF();
                        if(msg.startsWith("/reg")){
                            String[] tockens = msg.split(" ");
                            if(DBService.checkClient(tockens[1])){
                                sendMsg("Ник занят попробуйте друдой");
//                                DBService.logger(tockens[1], "register faild");
//                                LOGGER.error("Попытка регистрации ника " + tockens[1] + ". Ник занят.");
                            }else {
                                DBService.regNewClient(tockens[1], tockens[2], tockens[3]);
                                sendMsg("/regok");
//                                DBService.logger(tockens[1], "register");
//                                LOGGER.info("Регистрация нового клиента. НИК: " + tockens[1] );
                            }
                        }
                        if (msg.startsWith("/auth")) {
                            String[] tockens = msg.split(" ");
                            String newNick = DBService.getNickByLoginAndPass(tockens[1], tockens[2]);
                            if(serv.checkNick(newNick)){
                                sendMsg("Логин/ник занят. Введите другой логин");
//                                DBService.logger(nick, "logg faild");
//                                LOGGER.error("Логин/ник " + nick + " занят. Введите другой логин");

                            }
                            else if(newNick != null){
                                sendMsg("/authok");
                                nick = newNick;
                                serv.subscribe(ClientHandler.this);
//                                DBService.logger(nick, "logged in");
//                                LOGGER.info(nick + " connected");
                                break;
                            }else{
                                sendMsg("Неверный логин/пароль");
//                                LOGGER.error("Неверный логин/пароль");
                            }
                        }
                    }

                    while (true) {
                        String msg = in.readUTF();
                        if (msg.equals("/end")) {
                            out.writeUTF("/serverClosed");
//                            DBService.logger(nick, "logged out");
//                            LOGGER.info(nick + " вышел из чата");
                            break;
                        }
                        if(msg.startsWith("/w")) {
                            serv.sendPrivateMsg(nick, msg);
                            String[] tockens = msg.split(" ", 3);
//                            LOGGER.info(nick + " отправил личное сообщение " + tockens[1]);
                        }else if(msg.startsWith("/bl")){
                            String tockens[] =msg.split(" ");
                            if(DBService.getIdByNickname(tockens[1]) != null){
                                DBService.addToBlackList(nick, tockens[1]);
                                sendMsg("Пользователь: " + tockens[1] + " в черном списке.");
                                String log = "add " + tockens[1] + " to blacklist";
//                                DBService.logger(nick, log);
//                                LOGGER.info(nick + " " + log);
                                serv.broadcastClientsList();
                            }else sendMsg("Вы хотите добавить в черный список несуществующего пользователя");
//                            LOGGER.error("Попытка добавить в черный список пользователем " + nick + " несуществующего клиента");
                        }else serv.broadcastMsg(nick + " " +nick + ": " + msg);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
//                    LOGGER.warn("Что-то пошло не так");
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }serv.unsubscribe(ClientHandler.this);
                }
            }
        }).start();
    } catch (IOException e) {
        e.printStackTrace();
    }
}


    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
