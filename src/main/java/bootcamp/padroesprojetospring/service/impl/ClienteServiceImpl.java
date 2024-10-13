package bootcamp.padroesprojetospring.service.impl;

import bootcamp.padroesprojetospring.model.Cliente;
import bootcamp.padroesprojetospring.model.ClienteRepository;
import bootcamp.padroesprojetospring.model.Endereco;
import bootcamp.padroesprojetospring.model.EnderecoRepository;
import bootcamp.padroesprojetospring.service.ClienteService;
import bootcamp.padroesprojetospring.service.ViaCepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private EnderecoRepository enderecoRepository;
    @Autowired
    private ViaCepService viaCepService;

    @Override
    public Iterable<Cliente> buscarTodos() {
        return clienteRepository.findAll();
    }

    @Override
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Cleinte não encontrado para o ID: " + id));
    }

    @Override
    public void inserir(Cliente cliente) {
        salvarClienteComCep(cliente);

    }

    @Override
    public void atualizar(Long id, Cliente cliente) {
        Optional<Cliente> clienteBd = clienteRepository.findById(id);
        if (clienteBd.isPresent()) {
            salvarClienteComCep(cliente);
        }
    }

    @Override
    public void deletar(Long id) {
        clienteRepository.deleteById(id);
    }

    @Cacheable("enderecos")
    public Endereco buscarCep(String cep) {
        return viaCepService.consultarCep(cep);
    }

    private void salvarClienteComCep(Cliente cliente) {
        String cep = cliente.getEndereco().getCep();

        Endereco endereco = enderecoRepository.findById(cep).orElseGet(() -> {
            Endereco novoEndereco = buscarCep(cep);
            if (novoEndereco == null || novoEndereco.getCep() == null) {
                throw new IllegalArgumentException("CEP inválido ou não encontrado: " + cep);
            }
            enderecoRepository.save(novoEndereco);
            return novoEndereco;
        });
        cliente.setEndereco(endereco);
        clienteRepository.save(cliente);
    }
}
