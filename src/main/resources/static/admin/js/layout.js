document.addEventListener('DOMContentLoaded', () => {
    const sidebarContainer = document.getElementById('sidebar-container');
    if (!sidebarContainer) return;

    // Identifica qual página está ativa com base no URL atual
    const paginaAtual = window.location.pathname.split('/').pop() || 'index.html';

    sidebarContainer.innerHTML = `
        <aside class="sidebar">
            <div class="sidebar-brand">Biblioteca Admin</div>
            <ul class="sidebar-menu">
                <li><a href="index.html" class="${paginaAtual === 'index.html' ? 'active' : ''}">Painel</a></li>
                <li><a href="emprestimos.html" class="${paginaAtual === 'emprestimos.html' ? 'active' : ''}">Empréstimos</a></li>
                <li><a href="livros.html" class="${paginaAtual === 'livros.html' || paginaAtual === 'livro-form.html' ? 'active' : ''}">Livros</a></li>
                <li><a href="autores.html" class="${paginaAtual === 'autores.html' ? 'active' : ''}">Autores</a></li>
                <li><a href="usuarios.html" class="${paginaAtual === 'usuarios.html' || paginaAtual === 'usuario-form.html' || paginaAtual === 'usuario-detalhe.html' ? 'active' : ''}">Usuários</a></li>
                <li><a href="enderecos.html" class="${paginaAtual === 'enderecos.html' ? 'active' : ''}">Endereços</a></li>
                <li style="margin-top: 1rem;"><a href="/swagger-ui.html" target="_blank">Swagger API</a></li>
            </ul>
        </aside>
    `;
});