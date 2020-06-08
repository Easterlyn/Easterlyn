package com.easterlyn.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.Test;
import org.reflections.Reflections;

import static org.junit.Assert.fail;

public class CommandTest {

	// TODO require class/1 method annotated to be used as a command
	// TODO check tab completion
	// TODO require CommandSyntax and Description unless Default/HelpCommand

	@Test
	public void checkPermissions() {
		StringBuilder missingBasePerm = new StringBuilder();
		StringBuilder missingSubPerm = new StringBuilder();
		for (Class<?> clazz : new Reflections("com.easterlyn.command").getSubTypesOf(BaseCommand.class)) {
			if (Modifier.isAbstract(clazz.getModifiers())) {
				return;
			}
			System.out.println("Checking class " + clazz.getSimpleName());
			boolean hasBasePermission = clazz.isAnnotationPresent(CommandPermission.class);
			if (!hasBasePermission) {
				if (missingBasePerm.length() > 0) {
					missingBasePerm.append(", ");
				}
				missingBasePerm.append(clazz.getSimpleName());
			}

			for (Method method : clazz.getDeclaredMethods()) {
				if ((method.isAnnotationPresent(Subcommand.class) || method.isAnnotationPresent(Default.class)) && !hasBasePermission) {
					if (missingSubPerm.length() > 0) {
						missingSubPerm.append(", ");
					}
					missingSubPerm.append(method.toString());
				}
				if (method.isAnnotationPresent(CommandAlias.class) && !method.isAnnotationPresent(CommandPermission.class)) {
					if (missingBasePerm.length() > 0) {
						missingBasePerm.append(", ");
					}
					missingBasePerm.append(method.toString());
				}
			}
		}

		if (missingBasePerm.length() > 0 || missingSubPerm.length() > 0) {
			fail("Missing command permissions: "
					+ (missingBasePerm.length() == 0 ? "" : "NoBasePerm: " + missingBasePerm.toString())
					+ (missingSubPerm.length() == 0 ? "" : "NoSubPerm: " + missingSubPerm.toString()));
		}
	}

}
