/**
 * This JAVA class JPA Entity for questions, and defined all getter
 * and setter methods for all it's attributes
 * @author  Anup Shanbhag (shanbhaganup@gmail.com)
 * @version 1.0
 * @since   2020-04-16
 */

package com.upgrad.quora.service.entity;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.apache.commons.lang3.builder.HashCodeExclude;
import org.apache.commons.lang3.builder.EqualsExclude;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "question")
@NamedQueries({
		@NamedQuery(name = "Questions.getById", query = "SELECT q FROM QuestionEntity q WHERE q.uuid=:questionId"),
		@NamedQuery(name = "Questions.fetchByUserId", query = "SELECT q FROM QuestionEntity q WHERE q.user=:user"),
		@NamedQuery(name = "Questions.fetchAll", query = "SELECT q FROM QuestionEntity q") })
public class QuestionEntity implements Serializable {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "uuid")
	@NotNull
	@Size(max = 200)
	private String uuid;

	@Column(name = "content")
	@NotNull
	@Size(max = 500)
	private String content;

	@Column(name = "date")
	@NotNull
	private LocalDate date;

	@ManyToOne
	@JoinColumn(name = "user_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@NotNull
	@ToStringExclude
	@HashCodeExclude
	@EqualsExclude
	private UserEntity user;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, Boolean.FALSE);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, Boolean.FALSE);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}
