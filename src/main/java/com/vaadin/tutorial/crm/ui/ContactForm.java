package com.vaadin.tutorial.crm.ui;

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import com.vaadin.tutorial.crm.backend.entity.Company;
import com.vaadin.tutorial.crm.backend.entity.Contact;

public class ContactForm extends FormLayout {

	private Contact contact;

	TextField firstName = new TextField("First name");
	TextField lastName = new TextField("Last name");
	EmailField email = new EmailField("Email");
	ComboBox<Contact.Status> status = new ComboBox<>("Status");
	ComboBox<Company> company = new ComboBox<>("Company");
	Button save = new Button("Save");
	Button delete = new Button("Delete");
	Button close = new Button("Cancel");

	Binder<Contact> binder = new BeanValidationBinder<>(Contact.class);

	public ContactForm(List<Company> list) {
		this();
		company.setItems(list);
		company.setItemLabelGenerator(Company::getName);
		status.setItems(Contact.Status.values());

	}

	public ContactForm() {
		addClassName("contact-form");
		binder.bindInstanceFields(this);

		add(firstName, lastName, email, company, status, createButtonsLayout());
	}

	/*
	 * private HorizontalLayout createButtonsLayout() {
	 * save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
	 * delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
	 * close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
	 * save.addClickShortcut(Key.ENTER); close.addClickShortcut(Key.ESCAPE); return
	 * new HorizontalLayout(save, delete, close); }
	 */

	public void setContact(Contact contact) {
		this.contact = contact;
		binder.readBean(contact);
	}

	// Events
	public static abstract class ContactFormEvent extends ComponentEvent<ContactForm> {
		private Contact contact;

		protected ContactFormEvent(ContactForm source, Contact contact) {
			super(source, false);
			this.contact = contact;
		}

		public Contact getContact() {
			return contact;
		}
	}

	public static class SaveEvent extends ContactFormEvent {
		SaveEvent(ContactForm source, Contact contact) {
			super(source, contact);
		}
	}

	public static class DeleteEvent extends ContactFormEvent {
		DeleteEvent(ContactForm source, Contact contact) {
			super(source, contact);
		}
	}

	public static class CloseEvent extends ContactFormEvent {
		CloseEvent(ContactForm source) {
			super(source, null);
		}
	}

	public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
			ComponentEventListener<T> listener) {
		return getEventBus().addListener(eventType, listener);
	}

	
	private Component createButtonsLayout() {
		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
		close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		save.addClickShortcut(Key.ENTER);
		close.addClickShortcut(Key.ESCAPE);
		
		// Adding event listeners to buttons
		save.addClickListener(event -> validateAndSave());
		delete.addClickListener(event -> fireEvent(new DeleteEvent(this, contact)));
		close.addClickListener(event -> fireEvent(new CloseEvent(this)));
		binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
		return new HorizontalLayout(save, delete, close);
	}

	private void validateAndSave() {
		try {
			binder.writeBean(contact);
			fireEvent(new SaveEvent(this, contact));
		} catch (ValidationException e) {
			e.printStackTrace();
		}
	}
}