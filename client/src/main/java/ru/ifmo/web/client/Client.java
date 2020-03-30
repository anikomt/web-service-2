package ru.ifmo.web.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.web.client.util.Command;
import ru.ifmo.web.database.entity.User;
import ru.ifmo.web.service.UsersService;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;


public class Client {
    public static <SQLException_Exception extends Throwable> void main(String... args) throws SQLException_Exception, IOException, SQLException {
        URL url = new URL("http://localhost:8080/users?wsdl");
        Users usersService = new Users(url);
        UsersService userPort = usersService.getUsersServicePort();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int currentState = 0;
        Command command;
        UserDTO userDTO;
        Long id;

        writeHelp();
        while (true) {
            currentState = readState(currentState, reader);
            if (currentState < 0 || currentState > Command.values().length) {
                System.out.print(">");
                continue;
            } else if (currentState == 0 ) {
                writeHelp();
                continue;
            }
            command = Command.values()[currentState - 1];
            switch (command) {
                case FIND_ALL:
                    userPort.findAll().stream().map(Client::userToString).forEach(System.out::println);
                    break;
                case FIND_BY_FILTERS:
                    System.out.println("\nЧтобы не применять фильтр, оставьте значение пустым");
                    id = readLong(reader);
                    userDTO = readUser(reader);
                    userPort.findWithFilters(id, userDTO.getLogin(), userDTO.getPassword(),
                            userDTO.getEmail(), userDTO.getGender(), userDTO.getRegisterDate())
                            .stream().map(Client::userToString).forEach(System.out::println);
                    break;
                case INSERT:
                    userDTO = readUser(reader);
                    System.out.println(userPort.insert(userDTO.getLogin(), userDTO.getPassword(),
                            userDTO.getEmail(), userDTO.getGender(), userDTO.getRegisterDate()));
                    break;
                case UPDATE:
                    id = readLong(reader);
                    userDTO = readUser(reader);
                    System.out.println(userPort.update(id, userDTO.getLogin(), userDTO.getPassword(),
                            userDTO.getEmail(), userDTO.getGender(), userDTO.getRegisterDate()));
                    break;
                case DELETE:
                    id = readLong(reader);
                    System.out.println(userPort.delete(id));
                    break;
                case QUIT:
                    return;
            }
        }
    }

    private static UserDTO readUser(BufferedReader reader) {
        System.out.println("login:");
        String login = readString(reader);
        System.out.println("password:");
        String password = readString(reader);
        System.out.println("email:");
        String email = readString(reader);
        System.out.println("gender:");
        Boolean gender = readBoolean(reader);
        System.out.println("registerDate(yyyy-mm-dd):");
        XMLGregorianCalendar registerDate = readDate(reader);
        return new UserDTO(login, password, email, gender, registerDate);
    }

    private static String readString(BufferedReader reader) {
        String trim = null;
        try {
            trim = reader.readLine().trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (trim.isEmpty()) {
            return null;
        }
        return trim;
    }

    private static XMLGregorianCalendar readDate(BufferedReader reader) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date rd = sdf.parse(reader.readLine());

            GregorianCalendar c = new GregorianCalendar();

            if (rd != null) {
                    c.setTime(rd);
                XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
                xmlGregorianCalendar.setTimezone(0);
                return xmlGregorianCalendar;
            } else {
                return null;
            }
        } catch (java.lang.Exception e) {
            return null;
        }
    }

    private static Long readLong(BufferedReader reader) {
        try {
            return Long.parseLong(reader.readLine());
        } catch (java.lang.Exception e) {
            return null;
        }
    }

    private static Boolean readBoolean(BufferedReader reader) {
        try {
            String s = reader.readLine();
            if (s.equals("")) {
                return null;
            }
            return Boolean.parseBoolean(s);
        } catch (java.lang.Exception e) {
            return null;
        }
    }

    private static int readState(int current, BufferedReader reader) {
        try {
            return Integer.parseInt(reader.readLine());
        } catch (java.lang.Exception e) {
            return -1;
        }
    }

    private static void writeHelp() {
        System.out.println("\nВыберите один из пунктов:");
        System.out.println("0. Вывести help");
        for (Command value : Command.values()) {
            System.out.println(1 + value.ordinal() + ". " + value.getHelp());
        }
    }

    private static String userToString(User user) {
        return "User{" +
                "id=" + user.getId() +
                ", login='" + user.getLogin() + '\'' +
                ", email='" + user.getEmail() + '\'' +
                ", gender=" + user.isGender() +
                ", registerDate=" + user.getRegisterDate() +
                '}';
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class UserDTO {
        private String login;
        private String password;
        private String email;
        private Boolean gender;
        private XMLGregorianCalendar registerDate;

        public UserDTO(String login, String password, String email, Boolean gender, XMLGregorianCalendar registerDate) {
        }
    }
}
