package jo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import jo.command.AddCommand;
import jo.command.CheckCommand;
import jo.command.Command;
import jo.command.DeleteCommand;
import jo.command.ExitCommand;
import jo.command.FindCommand;
import jo.command.ListCommand;
import jo.command.MarkCommand;
import jo.task.Deadline;
import jo.task.Event;
import jo.task.Task;

/**
 * Responsible for parsing user input and generating corresponding `Command` objects.
 * It interprets user commands and creates the appropriate commands to perform tasks in the `Jo` application.
 */
public class Parser {

    /**
     * Commands that act on a String (e.g. description of task)
     */
    protected enum StringCommands {
        todo {
            public Command perform(String input) {
                return new AddCommand(new Task(input, false));
            }
        },
        deadline {
            public Command perform(String input) throws JoException {
                if (!input.contains("/by")) {
                    throw new JoException("Please specify a deadline.");
                } else {
                    String[] description = input.split("/by", 2);
                    String taskName = description[0].trim();
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate deadline = LocalDate.parse(description[1].trim(), formatter);
                        return new AddCommand(new Deadline(taskName, false, deadline));
                    } catch (DateTimeParseException e) {
                        throw new JoException("Invalid date format. Please use yyyy-MM-dd.");
                    }
                }
            }
        },
        event {
            public Command perform(String input) throws JoException {
                if (!input.contains("/from") || !input.contains("/to")) {
                    throw new JoException("Please specify a start AND end date.");
                } else {
                    String[] description = input.split("/from", 2);
                    String[] dates = description[1].split("/to", 2);
                    String taskName = description[0].trim();
                    try {
                        LocalDate start = LocalDate.parse(dates[0].trim());
                        LocalDate end = LocalDate.parse(dates[1].trim());
                        return new AddCommand(new Event(taskName, false, start, end));
                    } catch (DateTimeParseException e) {
                        throw new JoException("Invalid date format. Please use yyyy-MM-dd with a valid date.");
                    }
                }
            }
        },

        check {
            public Command perform(String input) throws JoException {
                try {
                    LocalDate deadline = LocalDate.parse(input);
                    return new CheckCommand(deadline);
                } catch (DateTimeParseException e) {
                    throw new JoException("Invalid date format. Please use yyyy-MM-dd with a valid date.");
                }
            }
        },
        find {
            public Command perform(String input) {
                return new FindCommand(input);
            }
        };

        /**
         * Performs the parsing of the command based on the provided input.
         *
         * @param s The input string representing the command.
         * @return The corresponding `Command` object.
         * @throws JoException If there is an error parsing the command.
         */
        public abstract Command perform(String s) throws JoException;
    }

    /**
     * List of commands that act on integer values (eg. indices).
     */
    protected enum IntCommands {
        mark {
            @Override
            public Command perform(int... taskIndices) {
                return new MarkCommand(taskIndices, true);
            }
        },
        unmark {
            @Override
            public Command perform(int... taskIndices) {
                return new MarkCommand(taskIndices, false);
            }
        },
        delete {
            @Override
            public Command perform(int... taskIndices) {
                return new DeleteCommand(taskIndices);
            }
        };

        /**
         * Performs the parsing of the command based on the provided task index.
         *
         * @param taskIndices The index of the task associated with the command.
         * @return The corresponding `Command` object.
         */
        public abstract Command perform(int... taskIndices);
    }

    /**
     * Checks if a given input string is present in a specified enum class.
     *
     * @param input The input string to check.
     * @param enumClass The enum class to check against.
     * @param <E> The enum type.
     * @return `true` if the input string is found in the enum class, otherwise `false`.
     */
    public static <E extends Enum<E>> boolean isInEnum(String input, Class<E> enumClass) {
        for (E enumValue : enumClass.getEnumConstants()) {
            if (enumValue.name().equals(input)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses the user input and generates the corresponding `Command` object.
     *
     * @param input The user input command.
     * @return The `Command` object representing the parsed command.
     * @throws JoException If there is an error parsing the command.
     */
    public static Command parse(String input) throws JoException {
        String trimmedInput = input.trim();
        String[] parts = trimmedInput.split(" ", 2);
        String instruction = parts[0].trim();

        if (trimmedInput.isEmpty()) {
            throw new JoException("The command cannot be empty.");

        } else if (isInEnum(trimmedInput, StringCommands.class)) {
            throw new JoException(String.format("The description of a %s cannot be empty.", input.trim()));

        } else if (isInEnum(trimmedInput, IntCommands.class)) {
            throw new JoException(String.format("Please specify a valid task number to %s.", input.trim()));

        } else if (trimmedInput.equals("list")) {
            return new ListCommand();

        } else if (trimmedInput.equalsIgnoreCase("bye")) {
            return new ExitCommand();

        // Command is in the StringCommands enumeration
        } else if (isInEnum(instruction, StringCommands.class)) {
            String description = parts[1].trim();

            for (StringCommands t : StringCommands.values()) {
                if (t.name().equals(instruction)) {
                    return t.perform(description);
                }
            }

        // Command is in the IntCommands enumeration
        } else if (isInEnum(instruction, IntCommands.class)) {
            String[] values = parts[1].split(",");
            int[] taskIndices = new int[values.length];

            // Parse each value to an integer and store in the taskIndices array
            for (int i = 0; i < values.length; i++) {
                if (!values[i].trim().matches("[0-9]+")) {
                    throw new JoException("Please specify valid index/indices using integers.");
                }
                taskIndices[i] = Integer.parseInt(values[i].trim()) - 1;
            }

            for (IntCommands c : IntCommands.values()) {
                if (c.name().equals(instruction)) {
                    return c.perform(taskIndices);
                }
            }

        } else {
            throw new JoException("I'm sorry, but I don't know what that means :-(");
        }

        return new ExitCommand();
    }
}
