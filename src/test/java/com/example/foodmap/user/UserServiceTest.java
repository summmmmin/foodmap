package com.example.foodmap.user;

import com.example.foodmap.user.domain.User;
import com.example.foodmap.user.domain.UserRepository;
import com.example.foodmap.user.dto.UserResponse;
import com.example.foodmap.user.service.UserService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    private final UserRepository repo = mock(UserRepository.class);
    private final UserService service = new UserService(repo);

    @Test
    void getById_success() {
        var u = new User(1L, "id", "pw", "이름", "Y",
                LocalDateTime.now(), LocalDateTime.now());
        when(repo.findById(1L)).thenReturn(Optional.of(u));

        UserResponse resp = service.getById(1L);

        assertEquals(1L, resp.id());
        assertEquals("id", resp.loginId());
        assertEquals("이름", resp.username());
        assertEquals("Y", resp.useYn());
        verify(repo).findById(1L);
    }

    @Test
    void getById_whenNotFound_throwsNoSuchElementException() {
        when(repo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.getById(999L));
    }
}
