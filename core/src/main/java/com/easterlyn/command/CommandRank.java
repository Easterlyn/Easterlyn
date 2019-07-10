package com.easterlyn.command;

import com.easterlyn.user.UserRank;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface CommandRank {
	UserRank value() default UserRank.MEMBER;
}
