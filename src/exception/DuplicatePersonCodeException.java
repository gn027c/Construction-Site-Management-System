package exception;

/**
 * NGOẠI LỆ TRÙNG LẶP MÃ ĐỊNH DANH NHÂN SỰ (DUPLICATE PERSON CODE)
 * ----------------------------------------------------------------------------
 * PHÂN CÔNG: Trần Ngọc Anh Tuấn (SE201513) - Core Dev
 * NHÁNH GIT: tuantna-person-exceptions
 * 
 * NOTE DÀNH CHO TUẤN:
 * - VỊ TRÍ CẦN LÀM:
 *   1. Sử dụng exception này trong PersonRepository.java hoặc PersonnelService.java (phương thức addPerson).
 *   2. Kiểm tra nếu mã nhân sự (Person.getCode()) đã tồn tại trong danh sách thì ném exception này:
 *      throw new DuplicatePersonCodeException("Mã nhân sự đã tồn tại: " + code);
 */
public class DuplicatePersonCodeException extends SiteManagementException {

   public class PersonnelService {
 
    private final PersonRepository repository;
 
    public PersonnelService(PersonRepository repository) {
        this.repository = repository;
    }
 
    public void addPerson(Person person) throws DuplicatePersonCodeException {
        if (person == null) {
            throw new IllegalArgumentException("Person must not be null");
        }
        if (repository.existsByCode(person.getCode())) {
            throw new DuplicatePersonCodeException(person.getCode());
        }
        repository.save(person);
    }
 
    public List<Person> getAllPersons() {
        return repository.findAll();
    }
}
