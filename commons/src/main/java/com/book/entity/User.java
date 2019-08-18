package com.book.entity;


import lombok.Data;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.io.Serializable;
import java.util.Collection;

import java.util.List;


/**
 * @author wangqianlong
 * @create 2019-07-29 14:29
 */

@Data
public class User implements UserDetails, Serializable {

    private static final long serialVersionUID = 6338526705853492368L;

    private String userId;
    private String userName;
    private String userPassword;
    private String userAddress;
    private String userEmail;
    private String userPhone;



    private List<GrantedAuthority> authorityList;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorityList;
    }

    @Override
    public String getPassword() {
        return userPassword;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
