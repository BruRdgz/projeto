package com.bibliotecacultura.service;

import com.bibliotecacultura.dto.FuncionarioDTO;
import com.bibliotecacultura.entity.Funcionario;
import com.bibliotecacultura.exception.DuplicadoException;
import com.bibliotecacultura.exception.EntidadeNaoEncontradaException;
import com.bibliotecacultura.exception.NegocioException;
import com.bibliotecacultura.repository.EmprestimoRepository;
import com.bibliotecacultura.repository.FuncionarioRepository;
import com.bibliotecacultura.session.SessaoFuncionario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepo;
    private final EmprestimoRepository emprestimoRepo;

    public FuncionarioService(FuncionarioRepository funcionarioRepo,
                              EmprestimoRepository emprestimoRepo) {
        this.funcionarioRepo = funcionarioRepo;
        this.emprestimoRepo = emprestimoRepo;
    }

    // ------------------------------------------------------------------ LEITURA

    public List<FuncionarioDTO> listarTodos(String termo) {
        return funcionarioRepo.search(termo).stream()
                .map(this::toDTO)
                .toList();
    }

    public FuncionarioDTO buscarPorId(Long id) {
        return funcionarioRepo.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Funcionário #" + id + " não encontrado."));
    }

    /**
     * RF001 — valida credenciais e retorna um objeto de sessão.
     *
     * NOTA: senhas são comparadas em texto simples aqui como placeholder.
     * Quando adicionar Spring Security, substitua por:
     *   passwordEncoder.matches(senha, f.getSenhaHash())
     */
    public SessaoFuncionario autenticar(String matricula, String senha) {
        Funcionario f = funcionarioRepo.findByMatriculaAndAtivoTrue(matricula)
                .orElseThrow(() -> new NegocioException("Matrícula ou senha inválidos."));

        // TODO: passwordEncoder.matches(senha, f.getSenhaHash())
        if (!f.getSenhaHash().equals(senha)) {
            throw new NegocioException("Matrícula ou senha inválidos.");
        }

        return new SessaoFuncionario(f.getId(), f.getNome(), f.getMatricula(), f.getCargo());
    }

    // ----------------------------------------------------------------- ESCRITA

    /** RF014 — cadastrar funcionário. Somente BIBLIOTECARIO_ADM pode chamar este método. */
    @Transactional
    public FuncionarioDTO cadastrar(FuncionarioDTO dto) {
        if (!cpfValido(dto.getCpf())) {
            throw new NegocioException("CPF inválido.");
        }
        if (funcionarioRepo.existsByCpf(dto.getCpf())) {
            throw new DuplicadoException("CPF " + dto.getCpf() + " já cadastrado.");
        }
        if (funcionarioRepo.existsByMatricula(dto.getMatricula())) {
            throw new DuplicadoException("Matrícula " + dto.getMatricula() + " já cadastrada.");
        }
        if (dto.getSenha() == null || dto.getSenha().isBlank()) {
            throw new NegocioException("Senha é obrigatória no cadastro.");
        }

        Funcionario f = new Funcionario();
        f.setNome(dto.getNome());
        f.setCpf(dto.getCpf());
        f.setMatricula(dto.getMatricula());
        f.setCargo(Funcionario.Cargo.valueOf(dto.getCargo()));
        // TODO: f.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
        f.setSenhaHash(dto.getSenha());
        f.setAtivo(true);

        return toDTO(funcionarioRepo.save(f));
    }

    @Transactional
    public void deletar(Long id) {
        if (!funcionarioRepo.existsById(id)) {
            throw new EntidadeNaoEncontradaException("Funcionário #" + id + " não encontrado.");
        }
        if (emprestimoRepo.existsByFuncionarioId(id)) {
            throw new NegocioException("Não é possível excluir: funcionário possui empréstimo(s) associado(s).");
        }

        funcionarioRepo.deleteById(id);
    }

    private boolean cpfValido(String cpf) {
        if (cpf == null) return false;

        String digitos = cpf.replaceAll("\\D", "");
        if (digitos.length() != 11 || digitos.chars().distinct().count() == 1) {
            return false;
        }

        int primeiroDigito = calcularDigitoCpf(digitos, 9);
        int segundoDigito = calcularDigitoCpf(digitos, 10);

        return primeiroDigito == Character.getNumericValue(digitos.charAt(9))
                && segundoDigito == Character.getNumericValue(digitos.charAt(10));
    }

    private int calcularDigitoCpf(String digitos, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += Character.getNumericValue(digitos.charAt(i)) * (tamanho + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    /** RF015 — atualizar nome, cargo ou status ativo de um funcionário. */
    @Transactional
    public FuncionarioDTO alterar(Long id, FuncionarioDTO dto) {
        Funcionario f = funcionarioRepo.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Funcionário #" + id + " não encontrado."));

        f.setNome(dto.getNome());
        f.setCargo(Funcionario.Cargo.valueOf(dto.getCargo()));
        f.setAtivo(dto.isAtivo());

        // Atualiza senha somente se uma nova foi fornecida
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            // TODO: f.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
            f.setSenhaHash(dto.getSenha());
        }

        return toDTO(funcionarioRepo.save(f));
    }

    // --------------------------------------------------------------- MAPEAMENTO

    public FuncionarioDTO toDTO(Funcionario f) {
        FuncionarioDTO dto = new FuncionarioDTO();
        dto.setId(f.getId());
        dto.setNome(f.getNome());
        dto.setCpf(f.getCpf());
        dto.setMatricula(f.getMatricula());
        dto.setCargo(f.getCargo().name());
        dto.setAtivo(f.isAtivo());
        // nunca expor senhaHash
        return dto;
    }
}
