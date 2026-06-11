package com.bibliotecacultura.service;

import com.bibliotecacultura.dto.ClienteDTO;
import com.bibliotecacultura.entity.Cliente;
import com.bibliotecacultura.exception.DuplicadoException;
import com.bibliotecacultura.exception.EntidadeNaoEncontradaException;
import com.bibliotecacultura.exception.NegocioException;
import com.bibliotecacultura.repository.ClienteRepository;
import com.bibliotecacultura.repository.EmprestimoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepo;
    private final EmprestimoRepository emprestimoRepo;

    public ClienteService(ClienteRepository clienteRepo, EmprestimoRepository emprestimoRepo) {
        this.clienteRepo = clienteRepo;
        this.emprestimoRepo = emprestimoRepo;
    }

    // ------------------------------------------------------------------ READ

    public List<ClienteDTO> listarTodos() {
        return clienteRepo.findAllByAtivoTrueOrderByNomeAsc().stream()
                .map(this::toDTO)
                .toList();
    }

    public List<ClienteDTO> pesquisar(String termo) {
        if (termo == null || termo.isBlank()) return listarTodos();
        return clienteRepo.search(termo).stream()
                .map(this::toDTO)
                .toList();
    }

    public ClienteDTO buscarPorId(Long id) {
        return clienteRepo.findById(id)
                .filter(Cliente::isAtivo)
                .map(this::toDTO)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente #" + id + " não encontrado."));
    }

    public ClienteDTO buscarPorCpf(String cpf) {
        return clienteRepo.findByCpfAndAtivoTrue(cpf)
                .map(this::toDTO)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Nenhum cliente com CPF " + cpf + " encontrado."));
    }

    // ----------------------------------------------------------------- WRITE

    /** RF010 — register a new client. */
    @Transactional
    public ClienteDTO cadastrar(ClienteDTO dto) {
        if (clienteRepo.existsByCpf(dto.getCpf())) {
            throw new DuplicadoException("CPF " + dto.getCpf() + " já cadastrado.");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setCpf(dto.getCpf());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefone(dto.getTelefone());
        // status_situacional = SEM_MULTA and saldo_multa = 0 are set by entity defaults

        return toDTO(clienteRepo.save(cliente));
    }

    /** RF012 — update name, email, or phone. CPF is immutable. */
    @Transactional
    public ClienteDTO alterar(Long id, ClienteDTO dto) {
        Cliente cliente = clienteRepo.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente #" + id + " não encontrado."));

        cliente.setNome(dto.getNome());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefone(dto.getTelefone());

        return toDTO(clienteRepo.save(cliente));
    }

    /**
     * RF013 — soft-delete.
     * Blocked if client has active loans or outstanding fines.
     */
    @Transactional
    public void inativar(Long id) {
        Cliente cliente = clienteRepo.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente #" + id + " não encontrado."));

        if (emprestimoRepo.existsByClienteIdAndDataDevolucaoRealIsNull(id)) {
            throw new NegocioException("Não é possível inativar: cliente possui empréstimo(s) ativo(s).");
        }
        if (cliente.getSaldoMulta().compareTo(BigDecimal.ZERO) > 0) {
            throw new NegocioException("Não é possível inativar: cliente possui multa em aberto.");
        }

        cliente.setAtivo(false);
        clienteRepo.save(cliente);
    }

    // --------------------------------------------------------------- MAPPING

    public ClienteDTO toDTO(Cliente c) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(c.getId());
        dto.setNome(c.getNome());
        dto.setCpf(c.getCpf());
        dto.setEmail(c.getEmail());
        dto.setTelefone(c.getTelefone());
        dto.setStatusSituacional(c.getStatusSituacional().name());
        dto.setSaldoMulta(c.getSaldoMulta());
        return dto;
    }
}
