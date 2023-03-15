package com.example.friendofstudent.service;

import com.example.friendofstudent.Constants;
import com.example.friendofstudent.config.BotConfig;
import com.example.friendofstudent.model.*;
import com.example.friendofstudent.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.*;

@Component
public class FriendOfStudentBot extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskService service;
    private BotConfig botConfig;
    private List<AddSubjectProcess> addSubjectProcesses = new ArrayList<>();
    private List<AddTaskProcess> addTaskProcesses = new ArrayList<>();
    private List<FindTasksProcess> findTasksProcesses = new ArrayList<>();

    public FriendOfStudentBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Начать"));
        listOfCommands.add(new BotCommand("/help", "Помощь по использованию бота"));
        listOfCommands.add(new BotCommand("/add_subject", "Сохранить новую дисциплину"));
        listOfCommands.add(new BotCommand("/save_task", "Сохранить новый вопрос"));
        listOfCommands.add(new BotCommand("/find_tasks", "Поиск вопросов"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {

        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            if (messageText.startsWith("/")) {
                switch (messageText) {
                    case "/start":
                        registerUser(update.getMessage());
                        startCommandReceived(chat_id, update.getMessage().getChat().getFirstName());
                        break;
                    case "/help":
                        sendMessage(chat_id, Constants.HELP_TEXT);
                        break;
                    case "/add_subject":
                        killAllProcesses(chat_id);

                        addSubjectProcesses.add(new AddSubjectProcess(chat_id));
                        sendMessage(chat_id, Constants.GET_SUBJECT_COURSE);
                        break;
                    case "/save_task":
                        killAllProcesses(chat_id);

                        addTaskProcesses.add(new AddTaskProcess(chat_id));
                        getAllSubjectsCommandReceived(chat_id);
                        break;
                    case "/find_tasks":
                        killAllProcesses(chat_id);

                        findTasksProcesses.add(new FindTasksProcess(chat_id));
                        getAllSubjectsCommandReceived(chat_id);
                        break;
                    case "/stop_finding":
                        findTasksProcesses.remove(getFindTasksProcessById(chat_id));
                        sendMessage(chat_id, Constants.STOP_FINDING);
                        break;
                    default:
                        sendMessage(chat_id, "Неизвестная команда");
                }
            } else {
                if (isAddSubjectProcessStarted(chat_id)) {
                    AddSubjectProcess addSubjectProcess = getAddSubjectProcessByChatId(chat_id);
                    Subject subject = addSubjectProcess.getSubject();

                    if (!addSubjectProcess.isCourseSaved()) {
                        subject.setCourse(Integer.parseInt(messageText));
                        addSubjectProcess.setCourseSaved(true);

                        sendMessage(chat_id, Constants.GET_PROFESSOR_NAME);
                    } else if (!addSubjectProcess.isProfessorSaved()) {
                        subject.setProfessor(messageText);
                        addSubjectProcess.setProfessorSaved(true);

                        sendMessage(chat_id, Constants.GET_SUBJECT_NAME);
                    } else if (!addSubjectProcess.isSubjectNameSaved()) {
                        subject.setSubjectName(messageText);

                        service.saveSubject(subject);

                        addSubjectProcesses.remove(getAddSubjectProcessByChatId(chat_id));
                        sendMessage(chat_id, String.format(Constants.SUBJECT_SAVED, subject.getSubjectName()));
                    }
                } else if (isAddTaskProcessStarted(chat_id)) {
                    AddTaskProcess addTaskProcess = getAddTaskProcessByChatId(chat_id);
                    Task task = addTaskProcess.getTask();
                    if (!addTaskProcess.isSubjectSaved()) {
                        int id = Integer.parseInt(messageText);
                        Optional<Subject> subject = service.getSubjectById(id);

                        if (subject.isPresent()) {
                            task.setSubject(subject.get());
                            addTaskProcess.setSubjectSaved(true);

                            sendMessage(chat_id, Constants.ENTER_NUMBER_OF_QUESTION);
                        } else {
                            sendMessage(chat_id, Constants.CANT_FIND_SUBJECT);
                            getAllSubjectsCommandReceived(chat_id);
                        }
                    } else if (!addTaskProcess.isNumberSaved()) {
                        int number = Integer.parseInt(messageText);

                        task.setNumber(number);
                        addTaskProcess.setNumberSaved(true);

                        sendMessage(chat_id, Constants.ENTER_QUESTION);
                    } else if (!addTaskProcess.isQuestionSaved()) {
                        task.setQuestion(messageText);
                        addTaskProcess.setQuestionSaved(true);

                        sendMessage(chat_id, Constants.ENTER_ANSWER);
                    } else if (!addTaskProcess.isAnswerSaved()) {
                        task.setAnswer(messageText);

                        service.saveTask(task);

                        addTaskProcesses.remove(addTaskProcess);
                        sendMessage(chat_id, Constants.TASK_SAVED);
                    }
                } else if (isFindTasksProcessStarted(chat_id)) {
                    FindTasksProcess findTasksProcess = getFindTasksProcessById(chat_id);

                    if (!findTasksProcess.isSubjectSelected()) {
                        int id = Integer.parseInt(messageText);

                        Optional<Subject> subject = service.getSubjectById(id);

                        if (subject.isPresent()) {
                            findTasksProcess.setSubject(subject.get());
                            findTasksProcess.setSubjectSelected(true);

                            sendMessage(chat_id, Constants.FIND_TASKS);
                            getListOfTasksBySubject(chat_id, findTasksProcess.getSubject());
                        } else {
                            sendMessage(chat_id, Constants.CANT_FIND_SUBJECT);
                            getAllSubjectsCommandReceived(chat_id);
                        }
                    } else {
                        try {
                            int number = Integer.parseInt(messageText);
                            sendMessage(chat_id, service.getTaskByNumber(findTasksProcess.getSubject(), number).toString());
                            sendMessage(chat_id, Constants.CONTINUE_OR_STOP_FINDING);
                            getListOfTasksBySubject(chat_id, findTasksProcess.getSubject());
                        } catch (NumberFormatException e) {
                            sendMessage(chat_id, service.getTaskByQuestion(findTasksProcess.getSubject(), messageText).toString());
                            sendMessage(chat_id, Constants.CONTINUE_OR_STOP_FINDING);
                            getListOfTasksBySubject(chat_id, findTasksProcess.getSubject());
                        }
                    }
                }
            }
        }
    }

    private void killAllProcesses(long chatId) {
        if (isAddSubjectProcessStarted(chatId)) {
            addSubjectProcesses.remove(getAddSubjectProcessByChatId(chatId));
        }

        if (isAddTaskProcessStarted(chatId)) {
            addTaskProcesses.remove(getAddTaskProcessByChatId(chatId));
        }

        if (isFindTasksProcessStarted(chatId)) {
            findTasksProcesses.remove(getFindTasksProcessById(chatId));
        }
    }

    private void getListOfTasksBySubject(long chatId, Subject subject) {
        List<Task> allTasksBySubject = service.getAllTasksBySubject(subject);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Constants.LIST_OF_TASKS, subject.getSubjectName()) + "\n\n");

        for (Task task : allTasksBySubject) {
            sb.append(String.format(Constants.GET_QUESTION, task.getNumber(), task.getQuestion()));
        }

        sendMessage(chatId, sb.toString());
    }

    private boolean isFindTasksProcessStarted(long chatId) {
        for (FindTasksProcess findTasksProcess : findTasksProcesses) {
            if (findTasksProcess.getChatId() == chatId) {
                return true;
            }
        }

        return false;
    }

    private FindTasksProcess getFindTasksProcessById(long chatId) {
        for (FindTasksProcess findTasksProcess : findTasksProcesses) {
            if (findTasksProcess.getChatId() == chatId) {
                return findTasksProcess;
            }
        }

        return null;
    }

    private boolean isAddSubjectProcessStarted(long chatId) {
        for (AddSubjectProcess addSubjectProcess : addSubjectProcesses) {
            if (addSubjectProcess.getChatId() == chatId) {
                return true;
            }
        }

        return false;
    }

    private AddSubjectProcess getAddSubjectProcessByChatId(long chatId) {
        for (AddSubjectProcess addSubjectProcess : addSubjectProcesses) {
            if (addSubjectProcess.getChatId() == chatId) {
                return addSubjectProcess;
            }
        }

        return null;
    }

    private boolean isAddTaskProcessStarted(long chatId) {
        for (AddTaskProcess addTaskProcess : addTaskProcesses) {
            if (addTaskProcess.getChatId() == chatId) {
                return true;
            }
        }

        return false;
    }

    private AddTaskProcess getAddTaskProcessByChatId(long chatId) {
        for (AddTaskProcess addTaskProcess : addTaskProcesses) {
            if (addTaskProcess.getChatId() == chatId) {
                return addTaskProcess;
            }
        }

        return null;
    }

    private void startCommandReceived(long chat_id, String name) {
        String answer = String.format("Hello, %s, nice to meet you", name);
        sendMessage(chat_id, answer);
    }


    private void registerUser(Message msg) {
        if (!userRepository.existsById(msg.getChatId())) {
            Long chatId = msg.getChatId();
            Chat chat = msg.getChat();
            String firstName = chat.getFirstName();
            String lastName = chat.getLastName();
            String username = chat.getUserName();
            Timestamp registeredAt = new Timestamp(System.currentTimeMillis());

            User user = new User(chatId, firstName, lastName, username, registeredAt);
            userRepository.save(user);
        }
    }

    private void sendMessage(long chat_id, String textToSend) {
        SendMessage message = new SendMessage(String.valueOf(chat_id), textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }

    private void getAllSubjectsCommandReceived(long chat_id) {
        List<Subject> subjects = (List<Subject>) service.getAllSubjects();
        Collections.sort(subjects, new Comparator<Subject>() {
            @Override
            public int compare(Subject o1, Subject o2) {
                return o1.getCourse() - o2.getCourse();
            }
        });

        StringBuilder sb = new StringBuilder();
        sb.append(Constants.SELECT_SUBJECT + ":\n\n");

        for (Subject subject : subjects) {
            sb.append("id: " + subject.getId() + " - " + subject + "\n\n");
        }

        sendMessage(chat_id, sb.toString());
    }
}
