/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylar.internal.core.util.MylarStatusHandler;
import org.eclipse.mylar.internal.jira.ui.wizards.AddExistingJiraTaskWizard;
import org.eclipse.mylar.internal.jira.ui.wizards.JiraRepositorySettingsPage;
import org.eclipse.mylar.internal.jira.ui.wizards.NewJiraQueryWizard;
import org.eclipse.mylar.internal.tasklist.ui.TaskListUiUtil;
import org.eclipse.mylar.internal.tasklist.ui.views.RetrieveTitleFromUrlJob;
import org.eclipse.mylar.internal.tasklist.ui.views.TaskListView;
import org.eclipse.mylar.internal.tasklist.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.mylar.provisional.tasklist.AbstractQueryHit;
import org.eclipse.mylar.provisional.tasklist.AbstractRepositoryConnector;
import org.eclipse.mylar.provisional.tasklist.AbstractRepositoryQuery;
import org.eclipse.mylar.provisional.tasklist.AbstractRepositoryTask;
import org.eclipse.mylar.provisional.tasklist.ITask;
import org.eclipse.mylar.provisional.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.provisional.tasklist.TaskRepository;
import org.eclipse.swt.widgets.Display;
import org.tigris.jira.core.model.Issue;
import org.tigris.jira.core.model.filter.IssueCollector;

/**
 * @author Mik Kersten
 */
public class JiraRepositoryConnector extends AbstractRepositoryConnector {

	private static final String VERSION_SUPPORT = "3.3.1 and higher";

	private List<String> supportedVersions;
	
	/** Name initially given to new tasks. Public for testing */
	public static final String NEW_TASK_DESC = "New Task";

	public String getLabel() {
		return MylarJiraPlugin.JIRA_CLIENT_LABEL;
	}

	public String getRepositoryType() {
		return MylarJiraPlugin.JIRA_REPOSITORY_KIND;
	}

	public String toString() {
		return getLabel();
	}

	public ITask createTaskFromExistingId(TaskRepository repository, String id) {
		return null; 
//		JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);
//		if (server != null) {
//			FilterDefinition filter = new FilterDefinition();
//			filter.set
//			server.findIssues(new Filter, collector)
//		}
//		String url = repository.getUrl() + MylarJiraPlugin.ISSUE_URL_PREFIX + id;
//		String handle = AbstractRepositoryTask.getHandle(repository.getUrl().toExternalForm(), id);
//		JiraTask newTask = new JiraTask(handle, NEW_TASK_DESC, true);
//		MylarTaskListPlugin.getTaskListManager().getTaskList().addTaskToArchive(newTask);
//		retrieveTaskDescription(newTask);
//		return newTask;
	} 

	public AbstractRepositorySettingsPage getSettingsPage() {
		return new JiraRepositorySettingsPage();
	}

	public IWizard getQueryWizard(TaskRepository repository) {
		return new NewJiraQueryWizard(repository);
	}

	public IWizard getAddExistingTaskWizard(TaskRepository repository) {
		return new AddExistingJiraTaskWizard(repository);
	}

//	@Override
//	public void synchronize() {
//		Job synchronizeJob = new Job(LABEL_JOB_SYNCHRONIZE) {
//			@Override
//			protected IStatus run(IProgressMonitor monitor) {
//				refreshFilters();
//				return Status.OK_STATUS;
//			}
//		};
//		synchronizeJob.schedule();
//	}

//	@Override
//	public Job synchronize(ITask task, boolean forceUpdate, IJobChangeListener listener) {
//		// Sync for individual tasks not implemented
//		return null;
//		return new Job(LABEL_JOB_SYNCHRONIZE) {
//			@Override
//			protected IStatus run(IProgressMonitor monitor) {
//				refreshFilters();
//				return Status.OK_STATUS;
//			}
//		};
//	}

	@Override
	public void openEditQueryDialog(AbstractRepositoryQuery query) {
		JiraRepositoryQuery filter = (JiraRepositoryQuery) query;
		String title = "Filter: " + filter.getDescription();
		TaskListUiUtil.openUrl(title, title, filter.getQueryUrl());
	}

//	public void refreshFilters() {
//		for (AbstractRepositoryQuery query : MylarTaskListPlugin.getTaskListManager().getTaskList().getQueries()) {
//			if (query instanceof JiraFilter) {
//				((JiraFilter) query).refreshHits();
//			}
//		}
//	}

	/**
	 * Attempts to set the task pageTitle to the title from the specified url
	 */
	protected void retrieveTaskDescription(final ITask jiraTask) {

		try {
			RetrieveTitleFromUrlJob job = new RetrieveTitleFromUrlJob(jiraTask.getUrl()) {

				@Override
				protected void setTitle(final String pageTitle) {
					jiraTask.setDescription(pageTitle);

					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							if (TaskListView.getDefault() != null)
								TaskListView.getDefault().refreshAndFocus();
						}
					});
				}

			};

			job.schedule();

		} catch (RuntimeException e) {
			MylarStatusHandler.fail(e, "could not open task web page", false);
		}
	}

	@Override
	protected List<AbstractQueryHit> performQuery(AbstractRepositoryQuery repositoryQuery, final IProgressMonitor monitor, MultiStatus queryStatus) {
		if (!(repositoryQuery instanceof JiraRepositoryQuery)) {
			return Collections.emptyList();
		}
		final JiraRepositoryQuery jiraRepositoryQuery = (JiraRepositoryQuery)repositoryQuery;
		final List<AbstractQueryHit> hits = new ArrayList<AbstractQueryHit>();
//		jiraFilter.setRefreshing(true);
		try {
			TaskRepository repository = MylarTaskListPlugin.getRepositoryManager().getRepository(MylarJiraPlugin.JIRA_REPOSITORY_KIND, repositoryQuery.getRepositoryUrl());
			JiraServerFacade.getDefault().getJiraServer(repository).executeNamedFilter(jiraRepositoryQuery.getNamedFilter(), new IssueCollector() {

				public void done() {
//					jiraFilter.setRefreshing(false);
//					Display.getDefault().asyncExec(new Runnable() {
//						public void run() {
//							if (TaskListView.getDefault() != null)
//								TaskListView.getDefault().refreshAndFocus();
//						}
//					});
				}

				public boolean isCancelled() {
					return monitor.isCanceled();
				}

				public void collectIssue(Issue issue) {
					int issueId = new Integer(issue.getId());
					JiraFilterHit hit = new JiraFilterHit(issue, jiraRepositoryQuery.getRepositoryUrl(), issueId);
					hits.add(hit);
//					addHit(hit);
				}

				public void start() {

				}
			});

		} catch (Exception e) {
//			isRefreshing = false;
//			jiraFilter.setRefreshing(false);
//			JiraServerFacade.handleConnectionException(e);
			queryStatus.add(new Status(IStatus.OK, MylarTaskListPlugin.PLUGIN_ID, IStatus.OK, 
					"Could not log in to server: " + repositoryQuery.getRepositoryUrl()
					+ "\n\nCheck network connection.", e));
//			queryStatus.add(Status.CANCEL_STATUS);
			
		}
		queryStatus.add(Status.OK_STATUS);
		return hits;
	} 
	
//	@Override
//	public void synchronize(AbstractRepositoryQuery repositoryQuery, IJobChangeListener listener) {
//		if (repositoryQuery instanceof JiraFilter) {
//			((JiraFilter) repositoryQuery).refreshHits();
//		}
//	}

	public void requestRefresh(AbstractRepositoryTask task) {
		// Task refresh not implemented.
	}

	@Override
	public boolean canCreateTaskFromId() {
		return false;
	}

	@Override
	public boolean canCreateNewTask() {
		return false;
	}

	@Override
	public IWizard getNewTaskWizard(TaskRepository taskRepository) {
		return null;
	}

	@Override
	public List<String> getSupportedVersions() {
		if (supportedVersions == null) {
			supportedVersions = new ArrayList<String>();
			supportedVersions.add(VERSION_SUPPORT);
		}
		return supportedVersions;
	}

	@Override
	protected void updateOfflineState(AbstractRepositoryTask repositoryTask, boolean forceSync) {
		// ignore
	}
}