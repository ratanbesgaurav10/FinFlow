package com.finflow.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "full_name", nullable = false) private String fullName;
    @Column(name = "email", nullable = false, unique = true) private String email;
    @Column(name = "password", nullable = false) private String password;
    @Enumerated(EnumType.STRING) @Column(name = "role", nullable = false) private Role role = Role.USER;
    @Column(name = "enabled", nullable = false) private boolean enabled = true;
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.LAZY) private List<Transaction> sentTransactions = new ArrayList<>();
    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY) private List<Transaction> receivedTransactions = new ArrayList<>();

    public User() {}
    public User(Long id, String fullName, String email, String password, Role role, boolean enabled, List<Transaction> sentTransactions, List<Transaction> receivedTransactions) {
        this.id=id; this.fullName=fullName; this.email=email; this.password=password; this.role=role; this.enabled=enabled; this.sentTransactions=sentTransactions; this.receivedTransactions=receivedTransactions;
    }
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getPassword(){return password;} public void setPassword(String v){password=v;}
    public Role getRole(){return role;} public void setRole(Role v){role=v;}
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
    public List<Transaction> getSentTransactions(){return sentTransactions;} public void setSentTransactions(List<Transaction> v){sentTransactions=v;}
    public List<Transaction> getReceivedTransactions(){return receivedTransactions;} public void setReceivedTransactions(List<Transaction> v){receivedTransactions=v;}
    public static Builder builder(){return new Builder();}
    public static class Builder {
        private Long id; private String fullName,email,password; private Role role=Role.USER; private boolean enabled=true; private List<Transaction> sentTransactions=new ArrayList<>(), receivedTransactions=new ArrayList<>();
        public Builder id(Long v){id=v;return this;} public Builder fullName(String v){fullName=v;return this;} public Builder email(String v){email=v;return this;} public Builder password(String v){password=v;return this;} public Builder role(Role v){role=v;return this;} public Builder enabled(boolean v){enabled=v;return this;} public Builder sentTransactions(List<Transaction> v){sentTransactions=v;return this;} public Builder receivedTransactions(List<Transaction> v){receivedTransactions=v;return this;}
        public User build(){return new User(id,fullName,email,password,role,enabled,sentTransactions,receivedTransactions);}
    }
    public enum Role { USER, ADMIN }
}
