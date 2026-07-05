package com.stology.be.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityTest {

    @Test
    void isAbstractMappedSuperclass() {
        assertThat(Modifier.isAbstract(BaseEntity.class.getModifiers())).isTrue();
        assertThat(BaseEntity.class.isAnnotationPresent(MappedSuperclass.class)).isTrue();
    }

    @Test
    void isRegisteredWithAuditingEntityListener() {
        EntityListeners listeners = BaseEntity.class.getAnnotation(EntityListeners.class);

        assertThat(listeners).isNotNull();
        assertThat(listeners.value()).containsExactly(AuditingEntityListener.class);
    }

    @Test
    void createdAtField_isAnnotatedForCreationAuditing() throws NoSuchFieldException {
        Field field = BaseEntity.class.getDeclaredField("createdAt");

        assertThat(field.getType()).isEqualTo(LocalDateTime.class);
        assertThat(field.isAnnotationPresent(CreatedDate.class)).isTrue();
        Column column = field.getAnnotation(Column.class);
        assertThat(column).isNotNull();
        assertThat(column.name()).isEqualTo("created_at");
        assertThat(column.updatable()).isFalse();
    }

    @Test
    void updatedAtField_isAnnotatedForModificationAuditing() throws NoSuchFieldException {
        Field field = BaseEntity.class.getDeclaredField("updatedAt");

        assertThat(field.getType()).isEqualTo(LocalDateTime.class);
        assertThat(field.isAnnotationPresent(LastModifiedDate.class)).isTrue();
        Column column = field.getAnnotation(Column.class);
        assertThat(column).isNotNull();
        assertThat(column.name()).isEqualTo("updated_at");
    }

    @Test
    void deletedAtField_hasColumnMappingWithoutAuditingAnnotations() throws NoSuchFieldException {
        Field field = BaseEntity.class.getDeclaredField("deletedAt");

        assertThat(field.getType()).isEqualTo(LocalDateTime.class);
        assertThat(field.isAnnotationPresent(CreatedDate.class)).isFalse();
        assertThat(field.isAnnotationPresent(LastModifiedDate.class)).isFalse();
        Column column = field.getAnnotation(Column.class);
        assertThat(column).isNotNull();
        assertThat(column.name()).isEqualTo("deleted_at");
    }
}