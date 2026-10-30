const API_BASE_URL = ''; // Usa caminhos relativos servidos pelo Spring Boot

/**
 * Função central para chamadas HTTP com tratamento do ErroResponse
 */
async function apiRequest(endpoint, method = 'GET', body = null) {
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
    };

    if (body) {
        options.body = JSON.stringify(body);
    }

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);

        // Status 204 No Content (Exclusões com sucesso)
        if (response.status === 204) {
            return null;
        }

        const data = await response.json();

        if (!response.ok) {
            // Trata o objeto ErroResponse retornado pelo GlobalExceptionHandler
            let mensagemErro = data.mensagem || 'Ocorreu um erro ao processar a requisição.';

            // Se houver validação de campos (HTTP 400 com mapa de campos)
            if (data.campos) {
                const detalhesCampos = Object.entries(data.campos)
                    .map(([campo, msg]) => `• ${campo}: ${msg}`)
                    .join('\n');
                mensagemErro += `\n\nDetalhes dos campos:\n${detalhesCampos}`;
            }

            const errorObj = new Error(mensagemErro);
            errorObj.status = data.status || response.status;
            errorObj.detalhes = data;
            throw errorObj;
        }

        return data;
    } catch (error) {
        console.error(`[API Error] ${method} ${endpoint}:`, error);
        throw error;
    }
}

// Helpers para chamadas diretas
const API = {
    get: (endpoint) => apiRequest(endpoint, 'GET'),
    post: (endpoint, body) => apiRequest(endpoint, 'POST', body),
    put: (endpoint, body) => apiRequest(endpoint, 'PUT', body),
    patch: (endpoint, body = null) => apiRequest(endpoint, 'PATCH', body),
    delete: (endpoint) => apiRequest(endpoint, 'DELETE')
};