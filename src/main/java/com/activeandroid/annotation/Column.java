//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.activeandroid.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	String name();

	int length() default -1;

	boolean notNull() default false;

	Column.NullConflictAction onNullConflict() default Column.NullConflictAction.FAIL;

	Column.ForeignKeyAction onDelete() default Column.ForeignKeyAction.NO_ACTION;

	Column.ForeignKeyAction onUpdate() default Column.ForeignKeyAction.NO_ACTION;

	public static enum ForeignKeyAction {
		SET_NULL,
		SET_DEFAULT,
		CASCADE,
		RESTRICT,
		NO_ACTION;

		private ForeignKeyAction() {
		}
	}

	public static enum NullConflictAction {
		ROLLBACK,
		ABORT,
		FAIL,
		IGNORE,
		REPLACE;

		private NullConflictAction() {
		}
	}
}
