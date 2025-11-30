package com.example.lotteryeventsystem;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class UserTest {

    @Test
    public void testDefaultConstructor() {
        // Given
        User user = new User();
        // Then
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getName());
        assertNull(user.getEmail());
        assertNull(user.getPhone());
    }

    @Test
    public void testParameterizedConstructor() {
        // Given
        String id = "user123";
        String name = "John Doe";
        String email = "john.doe@example.com";
        // When
        User user = new User(id, name, email);
        // Then
        assertNotNull(user);
        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertNull(user.getPhone());
    }

    @Test
    public void testParameterizedConstructorWithNullValues() {
        // Given
        String id = "user123";
        String name = null;
        String email = null;
        // When
        User user = new User(id, name, email);
        // Then
        assertNotNull(user);
        assertEquals(id, user.getId());
        assertNull(user.getName());
        assertNull(user.getEmail());
        assertNull(user.getPhone());
    }

    @Test
    public void testSettersAndGetters() {
        // Given
        User user = new User();
        // When
        user.setId("user456");
        user.setName("Jane Smith");
        user.setEmail("jane.smith@example.com");
        user.setPhone("+1234567890");
        // Then
        assertEquals("user456", user.getId());
        assertEquals("Jane Smith", user.getName());
        assertEquals("jane.smith@example.com", user.getEmail());
        assertEquals("+1234567890", user.getPhone());
    }

    @Test
    public void testUserFieldUpdates() {
        // Given
        User user = new User("initial_id", "Initial Name", "initial@example.com");
        // When - Update all fields
        user.setId("updated_id");
        user.setName("Updated Name");
        user.setEmail("updated@example.com");
        user.setPhone("555-999-8888");
        // Then
        assertEquals("updated_id", user.getId());
        assertEquals("Updated Name", user.getName());
        assertEquals("updated@example.com", user.getEmail());
        assertEquals("555-999-8888", user.getPhone());
    }

    @Test
    public void testMultipleUserInstances() {
        // Given
        User user1 = new User("user1", "User One", "user1@example.com");
        User user2 = new User("user2", "User Two", "user2@example.com");
        User user3 = new User("user3", "User Three", "user3@example.com");
        // Then
        assertEquals("user1", user1.getId());
        assertEquals("User One", user1.getName());
        assertEquals("user1@example.com", user1.getEmail());
        assertEquals("user2", user2.getId());
        assertEquals("User Two", user2.getName());
        assertEquals("user2@example.com", user2.getEmail());
        assertEquals("user3", user3.getId());
        assertEquals("User Three", user3.getName());
        assertEquals("user3@example.com", user3.getEmail());
    }
}