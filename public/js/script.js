const API_URL = "/tasks";

document.addEventListener("DOMContentLoaded", () => {
    carregarTarefas();
    inicializarDragAndDrop();
});

async function adicionarTarefa(text, statusDestino) {
    try {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                description: text,
                status: statusDestino
            })
        });

        const novaTarefa = await response.json();
        criarCardHTML(novaTarefa);

    } catch (erro) {
        console.error("Não foi possível criar Task: ", erro);
    }
}

function ativarModoEdicao(botaoAdicionar) {

    botaoAdicionar.classList.add('d-none');
    botaoAdicionar.nextElementSibling.classList.remove('d-none');
    botaoAdicionar.nextElementSibling.focus();
}

function salvarAoClicarFora(textarea, idColuna) {

    if (textarea.value.trim().length !== 0) {
        adicionarTarefa(textarea.value, idColuna);
    }

    textarea.classList.add('d-none');
    textarea.previousElementSibling.classList.remove('d-none');
    textarea.value = '';
}

function inicializarDragAndDrop() {
    const colunas = ['TODO', 'IN_PROGRESS', 'DONE'];

    colunas.forEach(colunaId => {
        const container = document.getElementById(colunaId);

        new Sortable(container, {
            group: 'kanban',
            animation: 150,
            ghostClass: 'sortable-ghost',
            onEnd: function (evt) {
                const itemArrastado = evt.item;
                const colunaDestino = evt.to.id;
                const idTarefa = itemArrastado.dataset.id;

                console.log(`Tafera ${idTarefa} movida para ${colunaDestino}.`);

                if (evt.from.id !== evt.to.id) {
                    atualizarTarefa(idTarefa, { status: evt.to.id });
                }
            }
        });
    });
}

async function carregarTarefas() {
    try {
        const response = await fetch(API_URL);
        const tarefas = await response.json();

        //Limpa as colunas
        ['TODO', 'IN_PROGRESS', 'DONE'].forEach(id => document.getElementById(id).innerHTML = "");

        tarefas.forEach(tarefa => {

            criarCardHTML(tarefa);
        });
    } catch (erro) {
        console.error("Erro ao carregar: ", erro);
    }
}

async function atualizarTarefa(id, dadosParaAtualizar) {

    try {

        const response = await fetch(`${API_URL}/${id}`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dadosParaAtualizar)
        });

        console.log("Status atualizado com sucesso!");

    } catch (erro) {
        console.error("Erro na requisição: ", erro);
        alert("Não foi possível salvar a alteração.");
    }
}

async function deletarTarefa(id) {

    try {

        const response = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' }
        });

        const card = document.querySelector(`[data-id="${id}"]`);
        if (card) card.remove();

        console.log("Task deletada com sucesso!")
    } catch (erro) {
        console.error("Erro na requisição: ", erro);
    }

}

function criarCardHTML(tarefa) {
    const coluna = document.getElementById(tarefa.status);
    if (!coluna) return; // Se o status não existir no HTML, ignora

    const card = document.createElement('div');
    card.className = 'task-card';
    card.setAttribute('data-id', tarefa.id);
    card.innerHTML = `
        <strong>#${tarefa.id}</strong> - ${tarefa.description}
        <br>
        <small class="text-muted" style="font-size: 11px;">
            <i class="far fa-clock"></i> ${new Date(tarefa.createdAt).toLocaleDateString()}
        </small>
        <i class="fas fa-trash-alt delete-btn" onclick="deletarTarefa(${tarefa.id})"></i>
    `;

    coluna.appendChild(card);
}