package com.vaadin.tutorial.crm.ui;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.tutorial.crm.backend.entity.Company;
import com.vaadin.tutorial.crm.backend.entity.Contact;
import com.vaadin.tutorial.crm.backend.service.CompanyService;
import com.vaadin.tutorial.crm.backend.service.ContactService;

/**
 * A sample Vaadin view class.
 * <p>
 * To implement a Vaadin view just extend any Vaadin component and use @Route
 * annotation to announce it in a URL as a Spring managed bean. Use the @PWA
 * annotation make the application installable on phones, tablets and some
 * desktop browsers.
 * <p>
 * A new instance of this class is created for every new user and every browser
 * tab/window.
 */
@Route("")
@CssImport("./styles/shared-styles.css")
public class MainView extends VerticalLayout {

	private ContactService contactService;
	private Grid<Contact> grid = new Grid<>(Contact.class);
	private TextField filterText = new TextField();
	private ContactForm form;

	public MainView(ContactService contactService, CompanyService companyService) {
		this.contactService = contactService;
		addClassName("list-view");
		setSizeFull();

		configureFilter();
		configureGrid();

		form = new ContactForm(companyService.findAll());
		form.addListener(ContactForm.SaveEvent.class, this::saveContact);
		form.addListener(ContactForm.DeleteEvent.class, this::deleteContact);
		form.addListener(ContactForm.CloseEvent.class, e -> closeEditor());

		Div content = new Div(grid, form);
		content.addClassName("content");
		content.setSizeFull();

		add(filterText, content);
		add(grid);
		updateList();

		closeEditor();
	}

	private void saveContact(ContactForm.SaveEvent event) {
		contactService.save(event.getContact());
		updateList();
		closeEditor();
	}

	private void deleteContact(ContactForm.DeleteEvent event) {
		contactService.delete(event.getContact());
		updateList();
		closeEditor();
	}

	private void configureFilter() {
		filterText.setPlaceholder("Filter by name...");
		filterText.setClearButtonVisible(true);
		filterText.setValueChangeMode(ValueChangeMode.LAZY);
		filterText.addValueChangeListener(e -> updateList());
	}

	private void configureGrid() {
		grid.addClassName("contact-grid");
		grid.setSizeFull();
		grid.removeColumnByKey("company");
		grid.setColumns("firstName", "lastName", "email", "status");
		grid.addColumn(contact -> {
			Company company = contact.getCompany();
			return company == null ? "-" : company.getName();
		}).setHeader("Company");
		grid.getColumns().forEach(col -> col.setAutoWidth(true));

		// For allowing to edit the selected contact row
		grid.asSingleSelect().addValueChangeListener(event -> editContact(event.getValue()));
	}

	private void updateList() {
		grid.setItems(contactService.findAll(filterText.getValue().trim()));
	}

	private void closeEditor() {
		form.setContact(null);
		form.setVisible(false);
		removeClassName("editing");
	}

	public void editContact(Contact contact) {
		if (contact == null) {
			closeEditor();
		} else {
			form.setContact(contact);
			form.setVisible(true);
			addClassName("editing");
		}
	}
}
