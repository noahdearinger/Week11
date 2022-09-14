package projects;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

public class ProjectsApp {
	
	private ProjectService projectService = new ProjectService(); 
	private Scanner sc = new Scanner(System.in);
	private Project curProject;
	// @formatter:off
	// creates the list for options the user can select from
			private List<String> operations = List.of(
					"1) Add a project",
					"2) List projects",
					"3) Select a project",
					"4) Update project details",
					"5) Delete a project"
					);
			// @formatter:on

	public static void main(String[] args) {
		new ProjectsApp().processUserSelections();
	}
	
	/*
	 * decides whether the user input is valid, 
	 * or if they want to terminate the app
	 */
	private void processUserSelections() {
		boolean done = false;
		
		while (!done) {
			try {
				int selection = getUserSelection();
				
				switch(selection) { 
				case -1:
					done = exitMenu();
					break;	
				case 1:
					createProject();
					break;
				case 2: 
					listProjects();
					break;
				case 3:
					selectProject();
					break;
					
				case 4: 
					updateProjectDetails();
					break;
					
				case 5:
					deleteProject();
					break;
					
				default:
					System.out.println("\n" + selection + " is not a valid selection, please try again.");
					break;
			}	
			}
			catch (Exception e) {
				System.out.println("\nError: " + e + ": Please enter a valid number.");
				e.printStackTrace();
			}
		}

	}
	


	/*
	 * method that is called within switch of getUserSelection, that creates
	 * project table
	 */
	private void createProject() {
		String projectName = getStringInput("Enter the project name");
		BigDecimal estimatedHours = getDecimalInput("Enter the estimated length of project, in hours");
		BigDecimal actualHours = getDecimalInput("Enter the actual length of the project, in hours");
		Integer difficulty = getIntInput("Enter the difficulty level (1-5)");
		String notes = getStringInput("Enter any projects notes you may have");
		
		Project project = new Project();
		
		project.setProjectName(projectName);
		project.setEstimatedHours(estimatedHours);
		project.setActualHours(actualHours);
		project.setDifficulty(difficulty);
		project.setNotes(notes);
		
		Project dbProject = projectService.addProject(project);
		System.out.println("You have successfully created project: " + dbProject);
	}


	//option 2 of app, lists current projects
	private void listProjects() {
		List<Project> projects = projectService.fetchAllProjects();
		System.out.println("\nProjects:");
		
		//@formatter:off
		projects.forEach(project -> System.out.println("   " 
						+ project.getProjectId() 
						+ ":   " 
						+ project.getProjectName()));
		//@formatter:on
	}
	
	//option 3 of the app, selects project to allow you to edit details
	private void selectProject() {
		listProjects();
		 Integer projectId = getIntInput ("Enter a project ID to select project");
		 
		 //deselects current project, if any
		 curProject = null;
		
		 //throws NoSuchElement e if invalid projectId entered
		 curProject = projectService.fetchProjectById(projectId);
		
	}
	

	private void updateProjectDetails() {
		if(Objects.isNull(curProject)) {
			System.out.println("\nPlease select a project.");
			return;
		}
		
		String projectName = getStringInput("Enter the project name [" 
				+ curProject.getProjectName() + "]");
		
		BigDecimal estimatedHours = getDecimalInput("Enter the new estimated hours of the project [" 
				+ curProject.getEstimatedHours() + "]");
		
		BigDecimal actualHours = getDecimalInput("Enter the new actual hours of the project ["
				+ curProject.getActualHours() + "]");
		
		Integer difficulty = getIntInput("Enter the new difficulty (1-5) [" 
		+ curProject.getDifficulty() + "]");
		
		String notes = getStringInput("Update notes? [" + curProject.getNotes() + "]");
		
		Project project = new Project();
		project.setProjectId(curProject.getProjectId());
		
		project.setProjectName(Objects.isNull(projectName) ? curProject.getProjectName() : projectName);
		project.setEstimatedHours(Objects.isNull(estimatedHours) ? curProject.getEstimatedHours() : estimatedHours);
		project.setActualHours(Objects.isNull(actualHours) ? curProject.getActualHours() : actualHours);
		project.setDifficulty(Objects.isNull(difficulty) ? curProject.getDifficulty() : difficulty);
		project.setNotes(Objects.isNull(notes) ? curProject.getNotes() : notes);
	
		projectService.modifyProjectDetails(project);
		curProject = projectService.fetchProjectById(curProject.getProjectId());
	}
	

	private void deleteProject() {
		listProjects();
		
		Integer projectId = getIntInput("Enter the ID for the project to delete");
		
		projectService.deleteProject(projectId);
		System.out.println("project " + projectId + " was successfully deleted.");
		
		if(Objects.nonNull(curProject) && curProject.getProjectId().equals(projectId)) {
			curProject = null;
		}
	}


	/*
	 * method that requests int input from user, then checks to see
	 *  if input is null
	 */
	private int getUserSelection() {
		printOperations();
		Integer input = getIntInput("Enter a menu selection");
		return Objects.isNull(input) ? -1 : input;
	}

	/*
	 * method that converts string input to integer, 
	 * if invalid throws numberformatexception
	 */
	private Integer getIntInput(String prompt) {
		String input = getStringInput(prompt);
		
		if(Objects.isNull(input)) {
			return null;
		}
		try {
			return Integer.valueOf(input);
		} 
		catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid number. Try again");
		}
	}

	// method to convert decimal input to BigDecimal
	private BigDecimal getDecimalInput(String prompt) {
		String input = getStringInput(prompt);
		if (Objects.isNull(input)) {
			return null;
		}
		try {
			return new BigDecimal(input).setScale(2);
		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid decimal number.");
		}
	}
	
	
	// method to terminate app
		private boolean exitMenu() {
		System.out.println("Exiting the menu. Goodbye.");
			return true;
		}
	
		// lowest level input, just recieves input from user
	private String getStringInput(String prompt) {
		System.out.print(prompt + ": ");
		String input = sc.nextLine();
		return input.isBlank() ? null : input.trim();
	}

	// method that prints choices for user to choose from
	private void printOperations() {
		System.out.println("\nThese are the available selections. Press the Enter key to quit: ");
		operations.forEach(line -> System.out.println("   " + line));

		if(Objects.isNull(curProject)) {
			System.out.println("\nYou are not working with a project.");
		} else {
			System.out.println("\nYou are working with project: " + curProject);
		}
	}

}
