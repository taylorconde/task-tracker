 package br.com.taylor;

 import br.com.taylor.entity.Task;
 import br.com.taylor.enums.TaskStatus;

 import java.nio.charset.StandardCharsets;

 //TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {
        Task estudo = new Task(1, "projeto java", TaskStatus.TODO);
        IO.println(estudo.toString());
    }
}
