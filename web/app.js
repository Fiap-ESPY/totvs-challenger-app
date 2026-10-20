const statusEl = document.querySelector("#status");
const formOportunidade = document.querySelector("#form-oportunidade");
const listaEl = document.querySelector("#lista-oportunidades");
const btnExcluir = document.querySelector("#btn-excluir");
const btnSalvar = document.querySelector("#btn-salvar");
const tituloForm = document.querySelector("#titulo-form");

let oportunidades = [];
let editandoId = null;

document.querySelectorAll(".aba").forEach((botao) => {
  botao.addEventListener("click", () => {
    document.querySelectorAll(".aba").forEach((item) => item.classList.remove("ativa"));
    document.querySelectorAll(".painel").forEach((item) => item.classList.remove("ativo"));
    botao.classList.add("ativa");
    document.querySelector("#painel-" + botao.dataset.aba).classList.add("ativo");
    if (botao.dataset.aba === "dashboard") {
      carregarDashboard();
    }
  });
});

formOportunidade.addEventListener("submit", salvarOportunidade);
document.querySelector("#btn-limpar").addEventListener("click", limparFormulario);
btnExcluir.addEventListener("click", excluirOportunidade);
document.querySelector("#form-reuniao").addEventListener("submit", registrarReuniao);
document.querySelector("#form-spiced").addEventListener("submit", salvarSpiced);
document.querySelector("#btn-briefing").addEventListener("click", gerarBriefing);
document.querySelector("#sel-spiced").addEventListener("change", carregarSpiced);
document.querySelector("#form-handoff").addEventListener("submit", calcularHandoff);
document.querySelector("#btn-dashboard").addEventListener("click", carregarDashboard);
document.querySelector("#filtro-bu").addEventListener("change", carregarDashboard);

carregarOportunidades();

async function api(caminho, opcoes = {}) {
  const resposta = await fetch(caminho, {
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    ...opcoes
  });
  const texto = await resposta.text();
  const corpo = texto ? JSON.parse(texto) : {};
  if (!resposta.ok) {
    throw new Error(corpo.erro || "Falha na requisição.");
  }
  return corpo;
}

function avisar(mensagem, erro = false) {
  statusEl.textContent = mensagem;
  statusEl.classList.toggle("erro", erro);
}

async function carregarOportunidades() {
  try {
    oportunidades = await api("/oportunidades");
    renderLista();
    preencherSelects();
    avisar(oportunidades.length + " oportunidade(s) no pipeline.");
  } catch (erro) {
    avisar(erro.message, true);
    listaEl.innerHTML = "<p class='vazio'>Não foi possível carregar o pipeline.</p>";
  }
}

function renderLista() {
  if (oportunidades.length === 0) {
    listaEl.innerHTML = "<p class='vazio'>Nenhuma oportunidade cadastrada.</p>";
    return;
  }
  const linhas = oportunidades.map((item) => `
    <tr data-id="${item.id}" class="${item.id === editandoId ? "selecionada" : ""}">
      <td>#${item.id}<br><strong>${escapar(item.cliente)}</strong></td>
      <td>${escapar(item.produto)}</td>
      <td>${escapar(item.buDescricao)}</td>
      <td>${escapar(item.etapaDescricao)}</td>
      <td>${item.risco ? "<span class='selo risco'>risco</span>" : "<span class='selo'>estável</span>"}</td>
    </tr>
  `).join("");
  listaEl.innerHTML = `
    <table class="tabela">
      <thead><tr><th>Cliente</th><th>Produto</th><th>BU</th><th>Etapa</th><th></th></tr></thead>
      <tbody>${linhas}</tbody>
    </table>`;
  listaEl.querySelectorAll("tr[data-id]").forEach((linha) => {
    linha.addEventListener("click", () => selecionar(Number(linha.dataset.id)));
  });
}

async function selecionar(id) {
  editandoId = id;
  const detalhe = await api("/oportunidades/" + id);
  formOportunidade.cliente.value = detalhe.cliente;
  formOportunidade.produto.value = detalhe.produto;
  formOportunidade.bu.value = detalhe.bu;
  formOportunidade.etapa.value = detalhe.etapa;
  tituloForm.textContent = "Editar oportunidade #" + id;
  btnSalvar.textContent = "Salvar alterações";
  btnExcluir.hidden = false;
  renderLista();
}

function limparFormulario() {
  editandoId = null;
  formOportunidade.reset();
  tituloForm.textContent = "Nova oportunidade";
  btnSalvar.textContent = "Cadastrar";
  btnExcluir.hidden = true;
  renderLista();
}

async function salvarOportunidade(evento) {
  evento.preventDefault();
  const dados = {
    cliente: formOportunidade.cliente.value,
    produto: formOportunidade.produto.value,
    bu: formOportunidade.bu.value,
    etapa: formOportunidade.etapa.value
  };
  try {
    if (editandoId == null) {
      await api("/oportunidades", { method: "POST", body: JSON.stringify(dados) });
      avisar("Oportunidade cadastrada.");
    } else {
      await api("/oportunidades/" + editandoId, {
        method: "PUT",
        body: JSON.stringify({ cliente: dados.cliente, produto: dados.produto, bu: dados.bu })
      });
      await api("/oportunidades/" + editandoId + "/etapa", {
        method: "PATCH",
        body: JSON.stringify({ etapa: dados.etapa })
      });
      avisar("Oportunidade atualizada.");
    }
    limparFormulario();
    await carregarOportunidades();
  } catch (erro) {
    avisar(erro.message, true);
  }
}

async function excluirOportunidade() {
  if (editandoId == null) return;
  try {
    await api("/oportunidades/" + editandoId, { method: "DELETE" });
    avisar("Oportunidade excluída.");
    limparFormulario();
    await carregarOportunidades();
  } catch (erro) {
    avisar(erro.message, true);
  }
}

function preencherSelects() {
  const opcoes = oportunidades.length === 0
    ? "<option value=''>Nenhuma oportunidade</option>"
    : oportunidades.map((item) => `<option value="${item.id}">#${item.id} ${escapar(item.cliente)}</option>`).join("");
  ["sel-reuniao", "sel-spiced", "sel-handoff"].forEach((id) => {
    const select = document.querySelector("#" + id);
    const atual = select.value;
    select.innerHTML = opcoes;
    if (atual) select.value = atual;
  });
  if (document.querySelector("#sel-spiced").value) {
    carregarSpiced();
  }
}

async function registrarReuniao(evento) {
  evento.preventDefault();
  const form = evento.currentTarget;
  const destino = document.querySelector("#resultado-reuniao");
  try {
    const reuniao = await api("/oportunidades/" + form.oportunidade.value + "/reunioes", {
      method: "POST",
      body: JSON.stringify({ transcricao: form.transcricao.value })
    });
    const insights = reuniao.insights.length === 0
      ? "<p class='vazio'>Nenhum insight automático.</p>"
      : reuniao.insights.map((item) => `
          <article class="insight">
            <span class="selo">${escapar(item.tipoDescricao)} · ${escapar(item.prioridade)}</span>
            <p>${escapar(item.descricao)}</p>
          </article>`).join("");
    destino.innerHTML = `<h2>Reunião #${reuniao.id}</h2><p>${escapar(reuniao.transcricao)}</p>${insights}`;
    avisar("Transcrição registrada.");
    await carregarOportunidades();
  } catch (erro) {
    avisar(erro.message, true);
    destino.innerHTML = `<p class="erro">${escapar(erro.message)}</p>`;
  }
}

async function carregarSpiced() {
  const id = document.querySelector("#sel-spiced").value;
  if (!id) return;
  try {
    const spiced = await api("/oportunidades/" + id + "/spiced");
    const form = document.querySelector("#form-spiced");
    form.situation.value = spiced.situation || "";
    form.pain.value = spiced.pain || "";
    form.impact.value = spiced.impact || "";
    form.criticalEvent.value = spiced.criticalEvent || "";
    form.decision.value = spiced.decision || "";
  } catch (erro) {
    avisar(erro.message, true);
  }
}

async function salvarSpiced(evento) {
  evento.preventDefault();
  const form = evento.currentTarget;
  try {
    await api("/oportunidades/" + form.oportunidade.value + "/spiced", {
      method: "PUT",
      body: JSON.stringify({
        situation: form.situation.value,
        pain: form.pain.value,
        impact: form.impact.value,
        criticalEvent: form.criticalEvent.value,
        decision: form.decision.value
      })
    });
    avisar("SPICED salvo.");
    await gerarBriefing();
  } catch (erro) {
    avisar(erro.message, true);
  }
}

async function gerarBriefing() {
  const id = document.querySelector("#sel-spiced").value;
  if (!id) return;
  try {
    const resposta = await api("/oportunidades/" + id + "/briefing");
    document.querySelector("#briefing").textContent = resposta.briefing;
  } catch (erro) {
    avisar(erro.message, true);
  }
}

async function calcularHandoff(evento) {
  evento.preventDefault();
  const id = evento.currentTarget.oportunidade.value;
  const destino = document.querySelector("#resultado-handoff");
  try {
    const handoff = await api("/oportunidades/" + id + "/handoff", { method: "POST" });
    destino.innerHTML = `
      <p class="card-numero">${handoff.score}<span style="font-size:18px">/100</span></p>
      <p>${escapar(handoff.classificacao)}</p>
      <p class="vazio">${handoff.camposSpicedPreenchidos} de 5 campos SPICED</p>
      <pre class="relatorio">${escapar(handoff.relatorio)}</pre>`;
    avisar("Handoff calculado.");
  } catch (erro) {
    avisar(erro.message, true);
    destino.innerHTML = `<p class="erro">${escapar(erro.message)}</p>`;
  }
}

async function carregarDashboard() {
  const bu = document.querySelector("#filtro-bu").value;
  const caminho = bu ? "/dashboard/" + bu : "/dashboard";
  try {
    const painel = await api(caminho);
    document.querySelector("#cards-dashboard").innerHTML = `
      <article class="cartao"><p class="vazio">Oportunidades</p><p class="card-numero">${painel.total}</p></article>
      <article class="cartao"><p class="vazio">Com risco</p><p class="card-numero">${painel.comRisco}</p></article>
      <article class="cartao"><p class="vazio">Gap de impacto</p><p class="card-numero">${painel.comImpactGap}</p></article>`;
    desenharBarras("#barras-bu", "Por unidade de negócio", painel.porBu, "descricao", "quantidade");
    desenharBarras("#barras-etapa", "Por etapa", painel.porEtapa, "descricao", "quantidade");
    avisar("Dashboard atualizado.");
  } catch (erro) {
    avisar(erro.message, true);
  }
}

function desenharBarras(seletor, titulo, grupos, rotulo, valor) {
  const maximo = Math.max(1, ...grupos.map((item) => item[valor]));
  const barras = grupos.filter((item) => item[valor] > 0).map((item) => `
    <div class="barra">
      <span>${escapar(item[rotulo])}</span>
      <div class="trilho"><span style="width:${(item[valor] / maximo) * 100}%"></span></div>
      <strong>${item[valor]}</strong>
    </div>`).join("");
  document.querySelector(seletor).innerHTML = `<h2>${titulo}</h2>${barras || "<p class='vazio'>Sem dados.</p>"}`;
}

function escapar(valor) {
  return String(valor ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");
}
