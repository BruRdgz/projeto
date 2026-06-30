package com.bibliotecacultura.service;

import com.bibliotecacultura.dto.LivroDTO;
import com.bibliotecacultura.entity.Categoria;
import com.bibliotecacultura.entity.Exemplar;
import com.bibliotecacultura.entity.Livro;
import com.bibliotecacultura.exception.EntidadeNaoEncontradaException;
import com.bibliotecacultura.exception.NegocioException;
import com.bibliotecacultura.repository.CategoriaRepository;
import com.bibliotecacultura.repository.ExemplarRepository;
import com.bibliotecacultura.repository.LivroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AcervoService {

    private final LivroRepository livroRepo;
    private final CategoriaRepository categoriaRepo;
    private final ExemplarRepository exemplarRepo;

    public AcervoService(LivroRepository livroRepo,
                         CategoriaRepository categoriaRepo,
                         ExemplarRepository exemplarRepo) {
        this.livroRepo = livroRepo;
        this.categoriaRepo = categoriaRepo;
        this.exemplarRepo = exemplarRepo;
    }

    // ------------------------------------------------------------------ READ

    public List<LivroDTO> listarTodos() {
        return livroRepo.findAllWithDetails().stream()
                .map(this::toDTO)
                .toList();
    }

    public List<LivroDTO> pesquisar(String termo) {
        if (termo == null || termo.isBlank()) return listarTodos();
        return livroRepo.search(termo).stream()
                .map(this::toDTO)
                .toList();
    }

    public LivroDTO buscarPorId(Long id) {
        return livroRepo.findByIdWithDetails(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Livro #" + id + " não encontrado."));
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepo.findAllByOrderByNomeAsc();
    }

    // ----------------------------------------------------------------- WRITE

    /** RF002 — register book + auto-create one DISPONIVEL exemplar. */
    @Transactional
    public LivroDTO cadastrar(LivroDTO dto) {
        Categoria categoria = categoriaRepo.findById(dto.getCategoriaId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Categoria não encontrada."));

        Livro livro = new Livro();
        livro.setTitulo(dto.getTitulo());
        livro.setAutor(dto.getAutor());
        livro.setAnoPublicacao(dto.getAnoPublicacao());
        livro.setCategoria(categoria);

        livro = livroRepo.save(livro);

        // RF002 step 16 — auto-create one DISPONIVEL copy
        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setStatus(Exemplar.Status.DISPONIVEL);
        exemplarRepo.save(exemplar);

        return toDTO(livro);
    }

    @Transactional
    public void cadastrarExemplar(Long livroId) {
        Livro livro = livroRepo.findById(livroId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Livro #" + livroId + " não encontrado."));

        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setStatus(Exemplar.Status.DISPONIVEL);
        exemplarRepo.save(exemplar);
    }

    @Transactional
    public void deletarExemplar(Long id) {
        Exemplar exemplar = exemplarRepo.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Exemplar #" + id + " não encontrado."));

        if (exemplar.getStatus() == Exemplar.Status.INDISPONIVEL) {
            throw new NegocioException("Não é possível excluir: exemplar está associado a um empréstimo ativo.");
        }

        exemplarRepo.delete(exemplar);
    }

    /** RF004 — update title, author, year, or category. */
    @Transactional
    public LivroDTO alterar(Long id, LivroDTO dto) {
        Livro livro = livroRepo.findByIdWithDetails(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Livro #" + id + " não encontrado."));

        Categoria categoria = categoriaRepo.findById(dto.getCategoriaId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Categoria não encontrada."));

        livro.setTitulo(dto.getTitulo());
        livro.setAutor(dto.getAutor());
        livro.setAnoPublicacao(dto.getAnoPublicacao());
        livro.setCategoria(categoria);

        return toDTO(livroRepo.save(livro));
    }

    /**
     * RF005 — delete book + all its copies.
     * Pre-condition: no copy may be currently loaned out.
     */
    @Transactional
    public void deletar(Long id) {
        if (!livroRepo.existsById(id)) {
            throw new EntidadeNaoEncontradaException("Livro #" + id + " não encontrado.");
        }
        if (exemplarRepo.existsByLivroIdAndStatus(id, Exemplar.Status.INDISPONIVEL)) {
            throw new NegocioException(
                    "Não é possível excluir: o livro possui exemplar(es) emprestado(s).");
        }
        livroRepo.deleteById(id);
    }

    // --------------------------------------------------------------- MAPPING

    public LivroDTO toDTO(Livro livro) {
        LivroDTO dto = new LivroDTO();
        dto.setId(livro.getId());
        dto.setTitulo(livro.getTitulo());
        dto.setAutor(livro.getAutor());
        dto.setAnoPublicacao(livro.getAnoPublicacao());
        dto.setCategoriaId(livro.getCategoria().getId());
        dto.setCategoriaNome(livro.getCategoria().getNome());
        dto.setStatusGeral(livro.getStatusGeral());
        return dto;
    }
}
