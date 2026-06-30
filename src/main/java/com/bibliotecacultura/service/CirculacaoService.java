package com.bibliotecacultura.service;

import com.bibliotecacultura.dto.EmprestimoDTO;
import com.bibliotecacultura.entity.*;
import com.bibliotecacultura.exception.EmprestimoInvalidoException;
import com.bibliotecacultura.exception.EntidadeNaoEncontradaException;
import com.bibliotecacultura.exception.NegocioException;
import com.bibliotecacultura.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CirculacaoService {

    /** Fine per overdue day — R$ 1.00 as a typical institutional rule. */
    private static final BigDecimal MULTA_POR_DIA = new BigDecimal("1.00");

    /** Maximum simultaneous active loans per client (RF006). */
    private static final int LIMITE_EMPRESTIMOS = 3;

    /** Loan duration in days (RF006 step 12). */
    private static final int DIAS_EMPRESTIMO = 14;

    /** Renewal extension in days (RF007 step 9). */
    private static final int DIAS_RENOVACAO = 7;

    private final EmprestimoRepository emprestimoRepo;
    private final ClienteRepository clienteRepo;
    private final LivroRepository livroRepo;
    private final ExemplarRepository exemplarRepo;
    private final FuncionarioRepository funcionarioRepo;

    public CirculacaoService(EmprestimoRepository emprestimoRepo,
                             ClienteRepository clienteRepo,
                             LivroRepository livroRepo,
                             ExemplarRepository exemplarRepo,
                             FuncionarioRepository funcionarioRepo) {
        this.emprestimoRepo  = emprestimoRepo;
        this.clienteRepo     = clienteRepo;
        this.livroRepo       = livroRepo;
        this.exemplarRepo    = exemplarRepo;
        this.funcionarioRepo = funcionarioRepo;
    }

    // ------------------------------------------------------------------ READ

    /** INT07 — active loans for a client, identified by CPF. */
    public List<EmprestimoDTO> listarAtivosDoCliente(String cpf) {
        Cliente cliente = clienteRepo.findByCpfAndAtivoTrue(cpf)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente com CPF " + cpf + " não encontrado."));
        return emprestimoRepo.findActivosByClienteId(cliente.getId()).stream()
                .map(this::toDTO)
                .toList();
    }

    /** INT08 — full loan history (active + returned) for a client. */
    public List<EmprestimoDTO> listarHistoricoDoCliente(String cpf) {
        Cliente cliente = clienteRepo.findByCpfAndAtivoTrue(cpf)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente com CPF " + cpf + " não encontrado."));
        return emprestimoRepo.findAllByClienteId(cliente.getId()).stream()
                .map(this::toDTO)
                .toList();
    }

    public EmprestimoDTO buscarEmprestimoAtivoPorExemplar(Long exemplarId) {
        return emprestimoRepo.findActivoByExemplarId(exemplarId)
                .map(this::toDTO)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Nenhum empréstimo ativo encontrado para o exemplar #" + exemplarId + "."));
    }

    // ----------------------------------------------------------------- WRITE

    /**
     * RF006 — register a new loan.
     *
     * Rules enforced:
     *  - Client must exist and be active
     *  - Client status must be SEM_MULTA (not COM_MULTA or BLOQUEADO)
     *  - Client must have fewer than 3 active loans
     *  - The requested book must have at least one DISPONIVEL copy
     *
     * @param funcionarioId  the logged-in librarian performing the operation
     */
    @Transactional
    public EmprestimoDTO registrarEmprestimo(String clienteCpf,
                                             Long livroId,
                                             Long funcionarioId) {
        Cliente cliente = clienteRepo.findByCpfAndAtivoTrue(clienteCpf)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Cliente com CPF " + clienteCpf + " não encontrado."));

        if (!cliente.podeEmprestar()) {
            throw new EmprestimoInvalidoException(
                    "Cliente bloqueado ou com multa pendente. Regularize a situação antes de realizar o empréstimo.");
        }

        long ativos = emprestimoRepo.countByClienteIdAndDataDevolucaoRealIsNull(cliente.getId());
        if (ativos >= LIMITE_EMPRESTIMOS) {
            throw new EmprestimoInvalidoException(
                    "Cliente já possui " + LIMITE_EMPRESTIMOS + " empréstimos ativos. Devolva um livro antes de pegar outro.");
        }

        Exemplar exemplar = exemplarRepo.findFirstAvailableByLivro(livroId)
                .orElseThrow(() -> new EmprestimoInvalidoException(
                        "Nenhum exemplar disponível para empréstimo neste momento."));

        Funcionario funcionario = funcionarioRepo.findById(funcionarioId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Funcionário não encontrado."));

        // Flip exemplar status
        exemplar.setStatus(Exemplar.Status.INDISPONIVEL);
        exemplarRepo.save(exemplar);

        // Create loan record
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setCliente(cliente);
        emprestimo.setExemplar(exemplar);
        emprestimo.setFuncionario(funcionario);
        emprestimo.setDataSaida(LocalDate.now());
        emprestimo.setDataPrevisao(LocalDate.now().plusDays(DIAS_EMPRESTIMO));

        return toDTO(emprestimoRepo.save(emprestimo));
    }

    /**
     * RF007 — renew a loan by extending its due date by 7 days.
     *
     * Rules enforced:
     *  - Loan must be active (not yet returned)
     *  - Client must have SEM_MULTA status
     */
    @Transactional
    public EmprestimoDTO renovarEmprestimo(Long emprestimoId) {
        Emprestimo emprestimo = emprestimoRepo.findById(emprestimoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Empréstimo #" + emprestimoId + " não encontrado."));

        if (!emprestimo.isAtivo()) {
            throw new EmprestimoInvalidoException("Este empréstimo já foi encerrado.");
        }

        Cliente cliente = emprestimo.getCliente();
        if (!cliente.podeEmprestar()) {
            throw new EmprestimoInvalidoException(
                    "Renovação negada: cliente com multa ou bloqueado.");
        }

        // Extend from current due date, not from today (RF007 step 9)
        emprestimo.setDataPrevisao(emprestimo.getDataPrevisao().plusDays(DIAS_RENOVACAO));

        return toDTO(emprestimoRepo.save(emprestimo));
    }

    /**
     * RF008 — register a book return.
     *
     * If the return is late, calculates the fine (R$ 1.00 per overdue day),
     * accumulates it on the client record, and updates their status.
     */
    @Transactional
    public EmprestimoDTO registrarDevolucao(Long exemplarId) {
        Emprestimo emprestimo = emprestimoRepo.findActivoByExemplarId(exemplarId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Nenhum empréstimo ativo encontrado para o exemplar #" + exemplarId + "."));

        LocalDate hoje = LocalDate.now();

        // Close the loan
        emprestimo.setDataDevolucaoReal(hoje);

        // Check for overdue and calculate fine
        if (hoje.isAfter(emprestimo.getDataPrevisao())) {
            long diasAtraso = ChronoUnit.DAYS.between(emprestimo.getDataPrevisao(), hoje);
            BigDecimal multa = MULTA_POR_DIA.multiply(BigDecimal.valueOf(diasAtraso));

            emprestimo.setMultaAplicada(multa);

            // Accumulate fine on client and update status
            Cliente cliente = emprestimo.getCliente();
            BigDecimal novoSaldo = cliente.getSaldoMulta().add(multa);
            cliente.setSaldoMulta(novoSaldo);
            cliente.setStatusSituacional(Cliente.StatusSituacional.COM_MULTA);
            clienteRepo.save(cliente);
        }

        // Free the copy
        Exemplar exemplar = emprestimo.getExemplar();
        exemplar.setStatus(Exemplar.Status.DISPONIVEL);
        exemplarRepo.save(exemplar);

        return toDTO(emprestimoRepo.save(emprestimo));
    }

    /**
     * RF009 — clear a client's fine after external payment.
     *
     * The librarian confirms payment was received out-of-band; this
     * zeroes the balance in the system and lifts the block.
     */
    @Transactional
    public void quitarMulta(String clienteCpf) {
        Cliente cliente = clienteRepo.findByCpfAndAtivoTrue(clienteCpf)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Cliente com CPF " + clienteCpf + " não encontrado."));

        if (cliente.getSaldoMulta().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("Cliente não possui multa em aberto.");
        }

        cliente.setSaldoMulta(BigDecimal.ZERO);
        cliente.setStatusSituacional(Cliente.StatusSituacional.SEM_MULTA);
        clienteRepo.save(cliente);
    }

    // --------------------------------------------------------------- MAPPING

    public EmprestimoDTO toDTO(Emprestimo e) {
        EmprestimoDTO dto = new EmprestimoDTO();
        dto.setId(e.getId());
        dto.setExemplarId(e.getExemplar().getId());
        dto.setClienteCpf(e.getCliente().getCpf());
        dto.setClienteNome(e.getCliente().getNome());
        dto.setLivroTitulo(e.getExemplar().getLivro().getTitulo());
        dto.setLivroAutor(e.getExemplar().getLivro().getAutor());
        dto.setDataSaida(e.getDataSaida());
        dto.setDataPrevisao(e.getDataPrevisao());
        dto.setDataDevolucaoReal(e.getDataDevolucaoReal());
        dto.setMultaAplicada(e.getMultaAplicada());
        dto.setAtivo(e.isAtivo());
        return dto;
    }
}
