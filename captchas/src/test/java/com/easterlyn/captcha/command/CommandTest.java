package com.easterlyn.captcha.command;

import static org.junit.Assert.fail;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.user.User;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.reflections.Reflections;

public class CommandTest {

  private static final List<Class<?>> requiringFlags = Arrays.asList(Player.class, User.class);

  @Test
  public void checkCommands() {
    StringJoiner permission = new StringJoiner(", ", "NoPerm: [", "]");
    StringJoiner description = new StringJoiner(", ", "NoDesc: [", "]");
    StringJoiner completion = new StringJoiner(", ", "NoCompletion: [", "]");
    StringJoiner syntax = new StringJoiner(", ", "NoSyntax: [", "]");
    StringJoiner flags = new StringJoiner(", ", "NeedsFlags: [", "]");
    for (Class<? extends BaseCommand> clazz :
        new Reflections(getClass().getPackage().getName()).getSubTypesOf(BaseCommand.class)) {
      if (Modifier.isAbstract(clazz.getModifiers())) {
        continue;
      }
      System.out.println("Checking class " + clazz.getSimpleName());
      checkAnnotation(clazz, CommandPermission.class, permission, true, true, false);
      checkAnnotation(clazz, Description.class, description, true, false, false);
      checkAnnotation(clazz, CommandCompletion.class, completion, false, false, true);
      checkAnnotation(clazz, Syntax.class, syntax, false, false, true);
      checkParamFlags(clazz, flags);
    }

    if (permission.length() > 10
        || description.length() > 10
        || completion.length() > 16
        || syntax.length() > 12
        || flags.length() > 14) {
      fail(
          new StringJoiner(" ", "Missing command annotations: ", "")
              .add(permission.toString())
              .add(description.toString())
              .add(completion.toString())
              .add(syntax.toString())
              .add(flags.toString())
              .toString());
    }
  }

  private void checkAnnotation(
      Class<?> clazz,
      Class<? extends Annotation> annotation,
      StringJoiner missing,
      boolean checkBase,
      boolean inheritBase,
      boolean requiresParameters) {
    boolean inherits = checkBase && inheritBase;
    if (checkBase
        && clazz.isAnnotationPresent(CommandAlias.class)
        && !clazz.isAnnotationPresent(annotation)) {
      missing.add(clazz.getSimpleName());
      if (inheritBase) {
        inherits = false;
      }
    }

    if (inherits) {
      return;
    }

    for (Method method : clazz.getDeclaredMethods()) {
      if ((method.isAnnotationPresent(Subcommand.class)
              || method.isAnnotationPresent(CommandAlias.class))
          && !method.isAnnotationPresent(annotation)
          && (!requiresParameters || method.getParameterCount() > 0)) {
        missing.add(method.toString());
      }
    }
  }

  private void checkParamFlags(Class<?> clazz, StringJoiner joiner) {
    for (Method method : clazz.getDeclaredMethods()) {
      if (!(method.isAnnotationPresent(Subcommand.class)
          || method.isAnnotationPresent(CommandAlias.class))) {
        continue;
      }
      for (Parameter parameter : method.getParameters()) {
        if (requiringFlags.stream()
                .anyMatch(flaggedClazz -> flaggedClazz.isAssignableFrom(parameter.getType()))
            && !parameter.isAnnotationPresent(Flags.class)) {
          joiner.add(method.toString() + '{' + parameter.toString() + '}');
        }
      }
    }
  }
}
