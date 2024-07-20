package com.vermeg.restapitestgenerator.services;

import com.vermeg.restapitestgenerator.models.User;
import com.vermeg.restapitestgenerator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UserDetailsImpl.build(user);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean deleteUserImage(User user) {
        String imageName = user.getImageName();
        if (imageName != null && !imageName.isEmpty()) {
            Path imagePath = Paths.get("public/images/", imageName);
            try {
                return Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    public boolean updateUserImage(User existingUser, String newImageName) {
        String oldImageName = existingUser.getImageName();
        if (oldImageName != null && !oldImageName.equals(newImageName)) {
            Path oldImagePath = Paths.get("public/images/", oldImageName);
            try {
                return Files.deleteIfExists(oldImagePath);
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    public void updateUserProfileImage(String username, String imageName) {
        Optional<User> userOptional = getUserByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setImageName(imageName);
            userRepository.save(user);
        }
    }

}
