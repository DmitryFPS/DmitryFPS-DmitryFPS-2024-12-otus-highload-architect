package ru.otus.orlov.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.otus.orlov.customers.Gender;


/** Сущность пользователя, представляющая информацию о пользователях в системе */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@NamedEntityGraph(name = "city-entity-graph", attributeNodes = {@NamedAttributeNode("city")})
@NamedEntityGraph(name = "city-roles-interests-token-entity-graph",
        attributeNodes = {@NamedAttributeNode("city"), @NamedAttributeNode("roles"),
                @NamedAttributeNode("interests"), @NamedAttributeNode("token")})
public class User implements UserDetails {
    /** Идентификатор пользователя */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Имя пользователя */
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    /** Фамилия пользователя */
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    /** Дата рождения пользователя */
    @Column(name = "birth_date", nullable = false)
    private Date birthDate;

    /** Пол пользователя */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender", nullable = false, columnDefinition = "gender_enum")
    private Gender gender;

    /** Интересы пользователя */
    @Fetch(FetchMode.SUBSELECT)
    @ManyToMany(targetEntity = Interest.class, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "user_interests",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id")
    )
    private Set<Interest> interests;

    /** Город, в котором проживает пользователь */
    @JoinColumn(name = "city_id")
    @ManyToOne(targetEntity = City.class, fetch = FetchType.LAZY)
    private City city;

    /** Email пользователя */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /** Пароль пользователя */
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    /** Роли клиента */
    @Fetch(FetchMode.SUBSELECT)
    @ManyToMany(targetEntity = Role.class, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /** Активный ли пользователь */
    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = Boolean.TRUE;

    /** Токен, связанный с пользователем */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "token_id", referencedColumnName = "id")
    private Token token;


    /** Роли для авторизации */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRole()))
                .collect(Collectors.toList());
    }

    /** Используем email как имя пользователя */
    @Override
    public String getUsername() {
        return email;
    }

    /** Учетная запись не просрочена */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** Учетная запись не заблокирована */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** Пароль не просрочен */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** Учетная запись активна */
    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
