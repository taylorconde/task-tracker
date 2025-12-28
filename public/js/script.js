// 1. Captura a lista do HTML
const taskList = document.getElementById('taskList');

// 2. Função para carregar as tarefas
function loadTasks() {
    fetch('/tasks')
        .then(response => response.json())
        .then(tasks => {
            // Limpa a lista antes de adicionar (para não duplicar)
            taskList.innerHTML = '';

            // 3. Itera sobre cada tarefa vinda do Java
            tasks.forEach(task => {
                const li = document.createElement('li');
                li.textContent = task.status + " - " + task.description + "  ";

                const button = document.createElement('button');
                button.innerText = "Excluir";
                button.addEventListener("click", () => deleteTask(task.id, li));


                li.appendChild(button);

                taskList.appendChild(li);
            });
        });
}

function deleteTask(id, element) {
    fetch('/tasks/' + id, { method: 'DELETE' })
        .then(response => {
            if (response.ok) {
                element.remove();
            }
        });
}

// 4. Chama a função assim que a página carregar
loadTasks();