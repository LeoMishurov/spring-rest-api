package leontyMishurov.users.rest;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import leontyMishurov.users.User;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public final class UserRestController {

    @NonNull
    private final JdbcOperations jdbcOperations;

    //Регистрация нового пользователя
    @PostMapping("registration")
    public ResponseEntity registration(@RequestParam String login, @RequestParam String password) {
        List<String> logins = this.jdbcOperations.query("SELECT login FROM public.users where login = ?",
                (resultSet, i) -> resultSet.getString("login"), login);
        if(!logins.isEmpty())
            return ResponseEntity.badRequest().build();

        jdbcOperations.update("INSERT INTO public.users (login, password) VALUES(?, ?);",
                login, password);
        return ResponseEntity.ok().build();
    }
    //Авторизация пользователя
    @GetMapping("avto")
    public ResponseEntity<Integer>  avto(@RequestParam String login,@RequestParam String password) {
        List<Integer> ids = this.jdbcOperations.query(
                "SELECT id FROM public.users where login = ? and password = ?",
                (resultSet, i) -> resultSet.getInt(1),
                login,password);

        if(ids.isEmpty())
            return ResponseEntity.badRequest().build();

        return ResponseEntity.ok(ids.get(0));
    }

    //поиск пользователей
    @GetMapping("find")
    public ResponseEntity<List<User>> find(@RequestParam String login){
        return ResponseEntity.ok ( this.jdbcOperations.query("SELECT id, login FROM public.users where login like ?",
                (resultSet, i) ->
                        new User(resultSet.getInt("id"),resultSet.getString("login")),
                "%"+login+"%"));

    }
    //Добавление пользователя в друзья
    @PostMapping("friend")
    public ResponseEntity friend(@RequestParam int userId, @RequestParam int friendId) {
        int countUsers = jdbcOperations.queryForObject("SELECT count(*) FROM public.users where id in (?, ?)",
                (resultSet, i) -> resultSet.getInt(1), userId, friendId);
        if(countUsers != 2)
            return ResponseEntity.badRequest().build();

        jdbcOperations.update("INSERT INTO public.friends (user_id, friend_id) VALUES(?, ?);", userId, friendId);
        return ResponseEntity.ok().build();
    }
    //Удаление из друзей
    @DeleteMapping("friend")
    public ResponseEntity DeleteFriend(@RequestParam int userId,@RequestParam int friendId) {
        int countUsers = jdbcOperations.queryForObject("SELECT count(*) FROM public.users where id in (?, ?)",
                (resultSet, i) -> resultSet.getInt(1), userId, friendId);
        if(countUsers != 2)
            return ResponseEntity.badRequest().build();

        jdbcOperations.update("DELETE FROM public.friends WHERE user_id = ? AND friend_id = ?", userId, friendId);
        return ResponseEntity.ok().build();
    }
    //Выыод списка друзей
    @GetMapping("listFriends")

    public ResponseEntity<List<User>> listFriends(@RequestParam int userId){
        return ResponseEntity.ok ( this.jdbcOperations.query("SELECT u.id, u.login from public.friends f " +
                        "left join public.users u on u.id = f.friend_id where f.user_id = ? ",
                (resultSet, i) ->
                        new User(resultSet.getInt("id"),resultSet.getString("login")),
                userId));

    }
}
