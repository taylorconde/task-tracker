
// 1. Captura a lista do HTML
const taskList = document.getElementById('taskList');
const taskInput = document.getElementById('taskInput');
const addTaskBtn = document.getElementById('addTaskBtn');

addTaskBtn.addEventListener('click', () => {
    const description = taskInput.value;

    if (description.trim() === "") {
        alert("Por favor, informe a tarefa.");
        return
    }

    addTask(description);
})

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

                // DESCRIPTION
                const descInput = document.createElement('input');
                descInput.type = 'text';
                descInput.value = task.description;

                descInput.addEventListener('change', () => {
                    updateTask(task.id, { description: descInput.value })
                })

                const statusSelect = document.createElement('select');
                statusSelect.innerHTML = `
                    <option value="TODO">A Fazer</option>
                    <option value="IN_PROGRESS">Em Andamento</option>
                    <option value="DONE">Concluída</option>`;
                statusSelect.value = task.status;

                statusSelect.addEventListener("change", () => {
                    updateTask(task.id, { status: statusSelect.value });
                });


                const deleteBtn = document.createElement('button');
                deleteBtn.innerText = "Excluir";
                deleteBtn.addEventListener("click", () => deleteTask(task.id, li));


                li.appendChild(descInput);
                li.appendChild(statusSelect);
                li.appendChild(deleteBtn);
                taskList.appendChild(li);
            });
        });
}

// 3. Função para deletar task
function deleteTask(id, element) {
    fetch('/tasks/' + id, { method: 'DELETE' })
        .then(response => {
            if (response.ok) {
                element.remove();
            }
        });
}

// 3.1 Função para adicionar task
function addTask(description) {
    const newTask = {
        description: description,
        status: 'TODO'
    };

    fetch('/tasks', {

        method: 'POST',
        headers: { 'Content-Type': 'application/json', },
        body: JSON.stringify(newTask)

    })
        .then(response => {
            if (response.ok) {
                loadTasks();
                taskInput.value = '';
            }
        });
}

// 3.2 Função para alterar task (PATCH)
function updateTask(id, changes) {

    fetch('/tasks/' + id, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(changes)
    })
        .then(response => {
            if (!response.ok) {
                alert("Erro ao atualizar tarefa");
            }
        });
}

// 4. Chama a função assim que a página carregar
loadTasks();