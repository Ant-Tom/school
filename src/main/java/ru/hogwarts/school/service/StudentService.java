package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.concurrent.CountDownLatch;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student addStudent(Student student) {
        logger.info("Was invoked method to add a new student");
        return studentRepository.save(student);
    }

    public Student findStudent(Long id) {
        logger.info("Was invoked method to find student with id {}", id);
        return studentRepository.findById(id).orElseThrow(() -> {
            logger.error("There is no student with id = {}", id);
            return new RuntimeException("Student not found");
        });
    }

    public List<String> getStudentNamesStartingWithA() {
        return studentRepository.findAll().stream()
                .map(Student::getName)
                .filter(name -> name.startsWith("А"))
                .map(String::toUpperCase)
                .sorted()
                .collect(Collectors.toList());
    }

    public double getAverageAgeOfStudents() {
        return studentRepository.findAll().stream()
                .mapToInt(Student::getAge)
                .average()
                .orElse(0.0);
    }

    public int getOptimizedSum() {
        int n = 1_000_000;
        return n * (n + 1) / 2;
    }

    public Student editStudent(Long id, Student student) {
        logger.info("Was invoked method to edit student with id {}", id);
        if (studentRepository.existsById(id)) {
            student.setId(id);
            return studentRepository.save(student);
        }
        logger.warn("Attempt to edit a non-existing student with id {}", id);
        return null;
    }

    public void deleteStudent(Long id) {
        logger.info("Was invoked method to delete student with id {}", id);
        studentRepository.deleteById(id);
    }

    public List<Student> findAllStudents() {
        logger.info("Was invoked method to get all students");
        return studentRepository.findAll();
    }

    public void printStudentsInParallel() {
        List<Student> students = studentRepository.findAll();
        if (students.size() < 6) {
            logger.warn("Not enough students to perform the task. At least 6 students required.");
            return;
        }

        // Инициализация CountDownLatch с количеством потоков (2 в данном случае)
        CountDownLatch latch = new CountDownLatch(2);

        // Первые два имени в основном потоке
        printStudentName(students.get(0));
        printStudentName(students.get(1));

        // Третий и четвертый в параллельном потоке
        new Thread(() -> {
            try {
                printStudentName(students.get(2));
                printStudentName(students.get(3));
            } finally {
                latch.countDown(); // Уменьшаем счетчик при завершении потока
            }
        }).start();

        // Пятый и шестой в еще одном параллельном потоке
        new Thread(() -> {
            try {
                printStudentName(students.get(4));
                printStudentName(students.get(5));
            } finally {
                latch.countDown(); // Уменьшаем счетчик при завершении потока
            }
        }).start();

        // Ожидание завершения всех потоков
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Восстанавливаем статус прерывания
            logger.error("The main thread was interrupted", e);
        }

        logger.info("All parallel threads have completed their execution");
    }


    public void printStudentName(Student student) {
        if (student != null) {
            System.out.println(student.getName());
        } else {
            logger.warn("Attempted to print a null student");
        }
    }


    public void printStudentsSynchronized() {
        List<Student> students = studentRepository.findAll();
        if (students.size() < 6) {
            logger.warn("Not enough students to perform the task. At least 6 students required.");
            return;
        }
        synchronizedPrint(students.get(0).getName());
        synchronizedPrint(students.get(1).getName());


        new Thread(() -> {
            synchronizedPrint(students.get(2).getName());
            synchronizedPrint(students.get(3).getName());
        }).start();


        new Thread(() -> {
            synchronizedPrint(students.get(4).getName());
            synchronizedPrint(students.get(5).getName());
        }).start();
    }

    private synchronized void synchronizedPrint(String name) {
        System.out.println(name);
    }

    public List<Student> findStudentsByAgeRange(int min, int max) {
        logger.info("Was invoked method to find students by age range {} - {}", min, max);
        return studentRepository.findByAgeBetween(min, max);
    }

    public long getTotalStudents() {
        logger.info("Was invoked method to get total number of students");
        return studentRepository.countAllStudents();
    }

    public double getAverageAge() {
        logger.info("Was invoked method to get average age of students");
        return studentRepository.getAverageStudentAge();
    }

    public List<Student> getLastFiveStudents() {
        logger.info("Was invoked method to get last five students");
        return studentRepository.findLastFiveStudents(PageRequest.of(0, 5));
    }

    public Faculty findFacultyOfStudent(Long studentId) {
        logger.info("Was invoked method to find faculty of student with id {}", studentId);
        Student student = findStudent(studentId);
        return student != null ? student.getFaculty() : null;
    }
}
