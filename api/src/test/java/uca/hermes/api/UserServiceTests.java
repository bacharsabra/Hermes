package uca.hermes.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import uca.hermes.api.service.impl.UserServiceImpl;
import uca.hermes.api.repo.UserRepository;
import uca.hermes.api.dao.User;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    public void setup() {
        user = new User("test", "password", false, null, null);
    }

    @Test
    public void testFindAll() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));

        List<User> result = userService.findAll();

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
    }

    @Test
    public void testFindById() {
        when(userRepository.findById("test")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById("test");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    public void testExistsById() {
        when(userRepository.existsById("test")).thenReturn(true);

        boolean result = userService.existsById("test");

        assertTrue(result);
    }

    @Test
    public void testSave() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.save(user);

        verify(userRepository, times(1)).save(user);
        verify(passwordEncoder, times(1)).encode("password"); // Change this line
    }

    @Test
    public void testDeleteById() {
        doNothing().when(userRepository).deleteById("test");

        userService.deleteById("test");

        verify(userRepository, times(1)).deleteById("test");
    }
}
